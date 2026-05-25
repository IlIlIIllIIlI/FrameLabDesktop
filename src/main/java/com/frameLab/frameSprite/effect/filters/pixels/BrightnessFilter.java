package com.frameLab.frameSprite.effect.filters.pixels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class BrightnessFilter extends PixelFilter implements AdjustableFilter {
    private double intensity;

    public BrightnessFilter() {
        this.intensity = getDefaultIntensity();
    }
    @Override
    public void setIntensity(double intensity) { this.intensity = intensity; }
    @Override
    public double getMinIntensity() { return -255.0; }
    @Override
    public double getMaxIntensity() { return 255.0; }
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

        int finalRed = (int) Math.clamp(red + this.intensity, 0, 255);
        int finalGreen = (int) Math.clamp(green + this.intensity, 0, 255);
        int finalBlue = (int) Math.clamp(blue + this.intensity, 0, 255);

        return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Brightness";
    }
}
