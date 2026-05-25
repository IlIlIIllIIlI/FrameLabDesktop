package com.frameLab.frameSprite.effect.filters.pixels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class ContrastFilter extends PixelFilter implements AdjustableFilter {
    double factor;

    public ContrastFilter(){
        this.setIntensity(getDefaultIntensity());
    }

    @Override
    public void setIntensity(double intensity) {
        this.factor = (259.0 * (intensity + 255.0)) / (255.0 * (259.0 - intensity));
    }
    @Override
    public double getMinIntensity() { return -100.0; }
    @Override
    public double getMaxIntensity() { return 100.0; }
    @Override
    public double getDefaultIntensity() { return 0.0; }

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
