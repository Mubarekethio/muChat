package org.mukee.chatapp.muchat.Model;

public class Requestss {


    public String user_name;
    public String user_status;// String? = null
    public String user_thumb_image;//void


    Requestss(){}

    Requestss(String user_name, String user_status, String user_thumb_image) {
        this.user_name = user_name;
        this.user_status = user_status;
        this.user_thumb_image = user_thumb_image;
    }
}
