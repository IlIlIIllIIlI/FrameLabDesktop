package com.frameLab.frameSprite.effect.filters.others;

import com.frameLab.frameSprite.effect.filters.Filter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ReflectFilter implements Filter {
    @Override
    public WritableImage apply(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage finalImage = new WritableImage(width, height);


        PixelReader reader = source.getPixelReader();
        PixelWriter writer = finalImage.getPixelWriter();

        int[][] originalArgb = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                originalArgb[y][x] = reader.getArgb(x,y);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setArgb(x, y, originalArgb[y][width-1-x]);
            }
        }

        return finalImage;
    }

    @Override
    public String getName() {
        return "Reflect";
    }
}
