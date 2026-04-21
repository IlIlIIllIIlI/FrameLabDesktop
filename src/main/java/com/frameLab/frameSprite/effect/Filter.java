package com.frameLab.frameSprite.effect;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public interface Filter {

        WritableImage apply(Image source);

        String getName();

}
