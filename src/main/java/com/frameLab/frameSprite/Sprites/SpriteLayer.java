package com.frameLab.frameSprite.Sprites;


import javafx.scene.image.WritableImage;

import java.io.Serializable;

public class SpriteLayer implements Serializable {
    public String name;

    public boolean isVisible = true;
    public double opacity = 1.0;

    public int x = 0;
    public int y = 0;

    public transient WritableImage image;

    public String storageFileName;

    public SpriteLayer(String name, int width, int height) {
        this.name = name;

        this.image = new WritableImage(width, height);
        this.storageFileName = "layer_" + this.name + ".png";
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }
}
