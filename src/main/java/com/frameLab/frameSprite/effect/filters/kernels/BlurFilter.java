package com.frameLab.frameSprite.effect.filters.kernels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class BlurFilter extends KernelFilter implements AdjustableFilter {
    private double intensity;

    public BlurFilter() { this.intensity = getDefaultIntensity(); }

    @Override
    public void setIntensity(double intensity) { this.intensity = intensity; }
    @Override
    public double getMinIntensity() { return 0.0; }
    @Override
    public double getMaxIntensity() { return 1.0; }
    @Override
    public double getDefaultIntensity() { return 0.5; }

    @Override
    protected double[][] getKernel() {
        double edge = intensity / 9.0;
        double center = (1.0 - intensity) + edge;

        return new double[][]{
                {edge, edge, edge},
                {edge, center, edge},
                {edge, edge, edge}
        };
    }

    @Override
    protected double getDivisor() {
        return 1.0;
    }

    @Override
    public String getName() {
        return "Blur";
    }
}
