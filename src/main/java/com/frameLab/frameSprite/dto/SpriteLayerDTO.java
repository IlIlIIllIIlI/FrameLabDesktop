package com.frameLab.frameSprite.dto;

import com.frameLab.frameSprite.Sprites.SpriteLayer;

import java.io.Serializable;

public class SpriteLayerDTO implements Serializable {
    public String name;

    public boolean isVisible;
    public double opacity;

    public int x = 0;
    public int y = 0;

    public String imageFileName;

   public SpriteLayerDTO(){

    }

    public SpriteLayerDTO(SpriteLayer spriteLayer){
        this.name = spriteLayer.getName();
        this.isVisible = spriteLayer.isVisible();
        this.opacity = spriteLayer.getOpacity();
        this.x = spriteLayer.getX();
        this.y = spriteLayer.getY();
        this.imageFileName = spriteLayer.getImageFileName();
    }

    public SpriteLayer toLayer(){
        SpriteLayer layer = new SpriteLayer(this.getName(),800,600);

        layer.setX(this.getX());
        layer.setY(this.getY());
        layer.setImageFileName(this.getImageFileName());
        layer.setOpacity(this.getOpacity());
        layer.setVisible(this.isVisible());

        return layer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
