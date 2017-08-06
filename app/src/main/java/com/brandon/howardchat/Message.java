package com.brandon.howardchat;

/**
 * Created by brandoncole on 8/2/17.
 */

public class Message {

    private String mContent;
    private String mUserId;
    private String mUserName;

    public Message(){
        mContent = null;
        mUserId = null;
        mUserName = null;
    }

    public Message(String content, String id, String name){
        mContent = content;
        mUserId = id;
        mUserName = name;
    }

    public String getmContent() {
        return mContent;
    }

    public String getmUserId() {
        return mUserId;
    }

    public String getmUserName() {
        return mUserName;
    }
}
