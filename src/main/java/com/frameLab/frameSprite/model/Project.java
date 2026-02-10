package com.frameLab.frameSprite.model;

public class Project {
    int id;
    String title;
    String imageUrl;

    public Project(int id, String title, String imageUrl){

        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(int id) {
        this.id = id;
    }
}
