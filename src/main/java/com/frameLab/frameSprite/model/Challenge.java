package com.frameLab.frameSprite.model;

public class Challenge {
    int id;
    String title;
    String description;
    String imageUrl;

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setTheme_title(String title) {
        this.title = title;
    }

    public void setRequired_picture_url(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTheme_description() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
