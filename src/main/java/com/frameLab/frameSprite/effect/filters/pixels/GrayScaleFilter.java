package com.frameLab.frameSprite.effect.filters.pixels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class GrayScaleFilter extends PixelFilter implements AdjustableFilter {

    private double intensity;

    public GrayScaleFilter() { this.intensity = getDefaultIntensity(); }
    @Override
    public void setIntensity(double intensity) { this.intensity = intensity; }
    @Override
    public double getMinIntensity() { return 0.0; }
    @Override
    public double getMaxIntensity() { return 1.0; }
    @Override
    public double getDefaultIntensity() { return 1.0; }


    @Override
    public int processPixel(int argb) {
            int alpha = (argb >> 24) & 0xFF;

            if (alpha == 0) {
                return argb;
            }

            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;

            int lum = (int) Math.round(red * 0.2126 + green * 0.7152 + blue * 0.0722);

            int finalRed = (int) (red * (1.0 - intensity) + lum * intensity);
            int finalGreen = (int) (green * (1.0 - intensity) + lum * intensity);
            int finalBlue = (int) (blue * (1.0 - intensity) + lum * intensity);

            return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Black & White";
    }
}
