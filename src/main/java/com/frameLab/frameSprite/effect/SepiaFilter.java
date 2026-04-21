package com.frameLab.frameSprite.effect;

public class SepiaFilter extends PixelFilter{
    @Override
    public int processPixel(int argb) {

        int alpha = (argb >> 24) & 0xFF;

        if (alpha == 0) {
            return argb;
        }

        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        int finalRed = Math.min((int) ((red *0.393)+(green *0.769)+(blue*0.189)),255);
        int finalGreen = Math.min((int) ((red *0.349)+(green *0.686)+(blue*0.168)),255);
        int finalBlue = Math.min((int) ((red *0.272)+(green *0.534)+(blue*0.131)),255);
        return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Sepia";
    }
}
