package com.frameLab.frameSprite.effect;

public class GrayScaleFilter implements Filter {
    @Override
    public int apply(int argb) {
            int a = (argb >> 24) & 0xFF;

            if (a == 0) {
                return argb;
            }

            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            int lum = (int) (r * 0.2126 + g * 0.7152 + b * 0.0722);

            return (a << 24) | (lum << 16) | (lum << 8) | lum;
    }

    @Override
    public String getName() {
        return "Black & White";
    }
}
