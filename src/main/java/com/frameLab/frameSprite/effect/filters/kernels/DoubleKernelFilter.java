package com.frameLab.frameSprite.effect.filters.kernels;

import com.frameLab.frameSprite.effect.filters.Filter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public abstract class DoubleKernelFilter implements Filter {

    protected abstract double[][] getKernelX();
    protected abstract  double[][] getKernelY();
    @Override
    public WritableImage apply(Image source) {
            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            WritableImage finalImage = new WritableImage(width, height);


            PixelReader reader = source.getPixelReader();
            PixelWriter writer = finalImage.getPixelWriter();

            double[][] kernelX = getKernelX();
            double[][] kernelY = getKernelY();
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                double blueX =0;
                double redX=0;
                double greenX=0;
                int alpha =((reader.getArgb(x,y)>> 24) & 0xFF);
                double blueY =0;
                double redY=0;
                double greenY=0;

                for (int i = -1; i <=1 ; i++) {
                    for (int j = -1; j <=1 ; j++) {
                        int pixel = reader.getArgb(x+j,y+i);
                        double weightX = kernelX[i+1][j+1];
                        double weightY = kernelY[i+1][j+1];

                        double blue = (pixel & 0xFF);
                        double green = ((pixel >> 8) & 0xFF);
                        double red =((pixel >> 16) & 0xFF);

                        redX += red*weightX;
                        blueX += blue*weightX;
                        greenX += green*weightX;

                        redY += red*weightY;
                        blueY += blue*weightY;
                        greenY += green*weightY;


                    }
                }

                int finalRed  = Math.min((int) Math.hypot(redX,redY),255);
                int finalBlue =  Math.min((int) Math.hypot(blueX,blueY),255);
                int finalGreen = Math.min((int) Math.hypot(greenX,greenY),255);

                int finalPixel = (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
                writer.setArgb(x,y,finalPixel);
            }
        }
        return finalImage;
    }

}
