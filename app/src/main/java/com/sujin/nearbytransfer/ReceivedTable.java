package com.sujin.nearbytransfer;

import android.net.Uri;

public class ReceivedTable {

    private String id;
    private String name;
    private String path;
    private String message;
    /*private String userName;
    private String time;
    private String location;*/


    public ReceivedTable(String id, String name, String message, String path)
    {

        this.id = id;
        this.name = name;
        this.path = path;
        this.message = message;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean checkID(String id, String name)
    {
        if(this.id.equals(id) && this.name.equals(name))
        {
            return true;
        }
        return false;
    }

    public String getMessage() {
        return message;
    }
}
