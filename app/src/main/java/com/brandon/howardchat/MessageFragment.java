package com.brandon.howardchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by brandoncole on 8/2/17.
 */

public class MessageFragment extends Fragment implements View.OnClickListener {

    ListView mListView;
    messageAdapter mMessageAdapter;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    EditText mMessageField;
    Button mSendButton;
    SwipeRefreshLayout mSwiperNoSwipping;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);

        // Button
        mSendButton = v.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(this);

        //EditText
        mMessageField = v.findViewById(R.id.message_field);

        // FireBase
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        //Set adapter
        mMessageAdapter = new messageAdapter(getActivity());

        //Setup Listview
        mListView = v.findViewById(R.id.list_view);
        mListView.setAdapter(mMessageAdapter);

        //Setup Swipper
        mSwiperNoSwipping = v.findViewById(R.id.swipe);
        mSwiperNoSwipping.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMessages();
            }
        });
        autoRefresh();

        // Make screen adjust when typing
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return v;
    }



    public interface messageListener {
        void onMessageResponse(List<Message> messageList);
    }

    public void onClick(View v) { // when button is clicked

        String content = mMessageField.getText().toString(); // get message

        if (!TextUtils.isEmpty(content)) {   // if there is a message

            FirebaseUser currentUser = mAuth.getCurrentUser(); // get user
            String userId = currentUser.getUid();   // get user id
            String userName = currentUser.getDisplayName();  // get user name

            Message messageObject = new Message(content, userId, userName); // put info into one object

            DatabaseReference messagesReference = mDatabaseRef.child("messages"); // get database
            messagesReference.push().setValue(messageObject);  // put message in database

            mMessageField.setText("");
        }
    }


    private void getMessage(messageListener messageListener){

        final messageListener internalMessageListener = messageListener;
        DatabaseReference messagesReference = mDatabaseRef.child("messages");

        messagesReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Message> messageObjectList = new ArrayList<Message>();

                for (DataSnapshot data : dataSnapshot.getChildren()){

                    Message messageObject = data.getValue(Message.class);

                    messageObjectList.add(messageObject);
                }

                internalMessageListener.onMessageResponse(messageObjectList);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void refreshMessages(){
        getMessage(new messageListener() {
            @Override
            public void onMessageResponse(List<Message> messageList) {
                mSwiperNoSwipping.setRefreshing(false);
                mMessageAdapter.setItems(messageList);
            }
        });
    }


    private void autoRefresh(){
        DatabaseReference dbf = FirebaseDatabase.getInstance().getReference();

        dbf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                refreshMessages();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class messageAdapter extends BaseAdapter{


        private Context mContext;
        private LayoutInflater mInflater;
        private List<Message> mDataSource;

        public messageAdapter(Context context) {
            mContext = context;
            mDataSource = new ArrayList<>();
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setItems(List<Message> messageList) {
            mDataSource.clear();
            mDataSource.addAll(messageList);
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return mDataSource.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Message message = mDataSource.get(i);
            View rowView = mInflater.inflate(R.layout.list_item_message, viewGroup, false);

            String content = message.getmContent();
            String name = message.getmUserName();

            TextView nameField = rowView.findViewById(R.id.name_field);
            TextView contentField = rowView.findViewById(R.id.content_field);

            nameField.setText(name);
            contentField.setText(content);

            return rowView;
        }
    }
}
