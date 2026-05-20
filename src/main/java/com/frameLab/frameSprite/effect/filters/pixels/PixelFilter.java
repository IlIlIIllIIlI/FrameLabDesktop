package com.frameLab.frameSprite.effect.filters.pixels;

import com.frameLab.frameSprite.effect.filters.Filter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public abstract class PixelFilter implements Filter {

    public WritableImage apply(Image source){
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage finalImage = new WritableImage(width, height);


        PixelReader reader = source.getPixelReader();
        PixelWriter writer = finalImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalArgb = reader.getArgb(x, y);

                int newArgb = processPixel(originalArgb);

                writer.setArgb(x, y, newArgb);
            }
        }

        return finalImage;
    }



    protected abstract int processPixel(int argb);
}
