package com.frameLab.frameSprite.effect.filters.pixels;

public class BrightnessFilter extends PixelFilter{
    int intensity;

    public BrightnessFilter(int intensity){
        this.intensity = intensity;
    }

    @Override
    public int processPixel(int argb) {
        int alpha = (argb >> 24) & 0xFF;

        if (alpha == 0) {
            return argb;
        }

        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        int finalRed = Math.clamp(red + this.intensity, 0, 255);
        int finalGreen = Math.clamp(green + this.intensity, 0, 255);
        int finalBlue = Math.clamp(blue + this.intensity, 0, 255);

        return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Brightness";
    }
}
