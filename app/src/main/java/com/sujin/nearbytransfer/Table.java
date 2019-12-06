package com.sujin.nearbytransfer;

public class Table {

    private String id;
    private String name;
    private String path;
    /*private String userName;
    private String time;
    private String location;*/


    public Table(String id, String name, String path)
    {

        this.id = id;
        this.name = name;
        this.path = path;

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

}
