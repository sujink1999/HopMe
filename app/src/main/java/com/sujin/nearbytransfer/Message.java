package com.sujin.nearbytransfer;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("msgid")
    private String msgid;

    @SerializedName("data")
    private String data;

    Message(String msgid,String data)
    {
        this.msgid = msgid;
        this.data = data;
    }
}
