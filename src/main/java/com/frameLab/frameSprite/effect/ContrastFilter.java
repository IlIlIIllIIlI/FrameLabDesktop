package com.frameLab.frameSprite.effect;

public class ContrastFilter extends PixelFilter{
    int intensity;
    double factor;

    public ContrastFilter(int intensity){
        this.intensity = intensity;
        this.factor = (259.0 * (intensity + 255.0)) / (255.0 * (259.0 - intensity));
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

        int finalRed = Math.clamp((int)(factor * (red - 128) + 128), 0, 255);
        int finalGreen = Math.clamp((int)(factor * (green - 128) + 128), 0, 255);
        int finalBlue = Math.clamp((int)(factor * (blue - 128) + 128), 0, 255);

        return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Contrast";
    }
}
