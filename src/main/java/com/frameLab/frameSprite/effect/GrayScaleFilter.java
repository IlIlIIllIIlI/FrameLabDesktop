package com.frameLab.frameSprite.effect;

public class GrayScaleFilter extends PixelFilter {
    @Override
    public int processPixel(int argb) {
            int alpha = (argb >> 24) & 0xFF;

            if (alpha == 0) {
                return argb;
            }

            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;

            int lum = (int) (red * 0.2126 + green * 0.7152 + blue * 0.0722);

            return (alpha << 24) | (lum << 16) | (lum << 8) | lum;
    }

    @Override
    public String getName() {
        return "Black & White";
    }
}
