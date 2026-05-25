package com.frameLab.frameSprite.effect.filters;

public interface AdjustableFilter extends Filter {
    void setIntensity(double intensity);
    double getMinIntensity();
    double getMaxIntensity();
    double getDefaultIntensity();
}