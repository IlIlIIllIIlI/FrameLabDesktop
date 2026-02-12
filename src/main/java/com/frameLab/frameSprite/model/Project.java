package com.frameLab.frameSprite.model;

import com.frameLab.frameSprite.Sprites.SpriteLayer;

import java.util.List;

public class Project {
    int id;
    String title;
    String imageUrl;
    int width;
    int height;
    List<SpriteLayer> layers;
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
}
