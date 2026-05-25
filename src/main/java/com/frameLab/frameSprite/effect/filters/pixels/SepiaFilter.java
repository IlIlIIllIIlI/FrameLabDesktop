package com.frameLab.frameSprite.effect.filters.pixels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class SepiaFilter extends PixelFilter implements AdjustableFilter {
    private double intensity;


    public SepiaFilter() { this.intensity = getDefaultIntensity(); }


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

        int sepiaRed = Math.min((int) ((red * 0.393) + (green * 0.769) + (blue * 0.189)), 255);
        int sepiaGreen = Math.min((int) ((red * 0.349) + (green * 0.686) + (blue * 0.168)), 255);
        int sepiaBlue = Math.min((int) ((red * 0.272) + (green * 0.534) + (blue * 0.131)), 255);

        int finalRed = (int) (red * (1.0 - intensity) + sepiaRed * intensity);
        int finalGreen = (int) (green * (1.0 - intensity) + sepiaGreen * intensity);
        int finalBlue = (int) (blue * (1.0 - intensity) + sepiaBlue * intensity);

        return (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    @Override
    public String getName() {
        return "Sepia";
    }
}
