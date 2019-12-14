package com.sujin.nearbytransfer;


import android.net.Uri;

public class SentTable {

    private String id;
    private String name;
    private Uri uri;
    private String message;
    /*private String userName;
    private String time;
    private String location;*/


    public SentTable(String id, String name, String message, Uri uri)
    {

        this.id = id;
        this.name = name;
        this.uri = uri;
        this.message = message;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public String getMessage() {
        return message;
    }

    public boolean checkID(String id, String name)
    {
        if(this.id.equals(id) && this.name.equals(name))
        {
            return true;
        }
        return false;
    }

}