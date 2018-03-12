package com.mokxa.learn.chatapp;

/**
 * Created by enterprise on 3/7/18.
 */

public class Users {

    public Users() {

    }

    public Users(String image,String name, String status, String thumb_image) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String name, image, status, thumb_image;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public void setThumbImage(String thumb) {
        this.thumb_image = thumb;
    }
}
