package com.brandon.howardchat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by brandoncole on 8/2/17.
 */

public class Retrieval {

    DatabaseReference mDataReference = FirebaseDatabase.getInstance().getReference();

    public interface ArticleListener {
        void onArticleResponse(List<Message> articleList);
    }

    public static void getMessages(){

    }

}
