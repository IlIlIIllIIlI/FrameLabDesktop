package com.frameLab.frameSprite.model;

import com.frameLab.frameSprite.Sprites.SpriteLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project {
    private int id;
    private String title;
    private String imageUrl;
    private int userId;
    private int challengeId;
    private int width;
    private int height;
    private List<SpriteLayer> layers;
    private Date lastModified;

    private Project(){
        this.id = 0;
        this.layers = new ArrayList<>();
    }
    public Project(int id, String title){

        this.id = id;
        this.title = title;
        this.layers = new ArrayList<>();

    }

    public Project(int id, String title,int userId,int challengeId,int width, int height){

        this.id = id;
        this.title = title;
        this.userId = userId;
        this.challengeId = challengeId;
        this.width = width;
        this.height = height;
        this.layers = new ArrayList<>();

    }




    public int getId() {
        return id;
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

    public List<SpriteLayer> getLayers() {
        return layers;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setLayers(List<SpriteLayer> layers) {
        this.layers = layers;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getUserId() {
        return userId;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
