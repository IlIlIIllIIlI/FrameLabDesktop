package com.frameLab.frameSprite.effect.filters.kernels;

import com.frameLab.frameSprite.effect.filters.Filter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public abstract class KernelFilter implements Filter {

    protected abstract double[][] getKernel();

    protected abstract double getDivisor();

    @Override
    public WritableImage apply(Image source){
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage finalImage = new WritableImage(width, height);


        PixelReader reader = source.getPixelReader();
        PixelWriter writer = finalImage.getPixelWriter();

        double[][] kernel = getKernel();
        double divisor = getDivisor();

        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                double blue =0;
                double red=0;
                double green=0;
                double alpha =0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <=1 ; j++) {
                        int pixel = reader.getArgb(x+j,y+ i);
                        double weight = kernel[i+1][j+1];

                        blue += (pixel & 0xFF)*weight;
                        green += ((pixel >> 8) & 0xFF)*weight;
                        red +=((pixel >> 16) & 0xFF)*weight;

                        alpha += ((pixel >> 24) & 0xFF)*weight;

                    }
                }

              int finalRed  = Math.min((int) Math.max((int) red/divisor,0),255);
              int finalBlue =  Math.min((int) Math.max((int) blue/divisor,0),255);
              int finalGreen = Math.min((int) Math.max((int) green/divisor,0),255);
              int finalAlpha = Math.min((int) Math.max((int) alpha/divisor,0),255);

              int finalPixel = (finalAlpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
              writer.setArgb(x,y,finalPixel);
            }
        }
        return finalImage;
    }
}
