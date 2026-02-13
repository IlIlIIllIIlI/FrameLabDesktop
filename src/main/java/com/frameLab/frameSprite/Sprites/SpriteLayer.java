package com.frameLab.frameSprite.Sprites;


import javafx.scene.image.WritableImage;


public class SpriteLayer {
    public String name;

    public boolean isVisible = true;
    public double opacity = 1.0;

    public int x = 0;
    public int y = 0;

    
    public WritableImage image;

    public String imageFileName;

    public SpriteLayer(String name, int width, int height) {
        this.name = name;

        this.image = new WritableImage(width, height);
        this.imageFileName = this.name + ".png";
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public WritableImage getImage() {
        return image;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public double getOpacity() {
        return opacity;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
