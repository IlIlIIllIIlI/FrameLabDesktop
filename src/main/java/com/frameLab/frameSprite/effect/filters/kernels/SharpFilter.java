package com.frameLab.frameSprite.effect.filters.kernels;

import com.frameLab.frameSprite.effect.filters.AdjustableFilter;

public class SharpFilter extends KernelFilter implements AdjustableFilter {

    private double intensity;

    public SharpFilter() { this.intensity = getDefaultIntensity(); }

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
        double edge = -intensity;
        double center = 1.0 + (4.0 * intensity);

        return new double[][]{
                {0, edge, 0},
                {edge, center, edge},
                {0, edge, 0}
        };
    }

    @Override
    protected double getDivisor() {
        return 1.0;
    }

    @Override
    public String getName() {
        return "Sharpen";
    }
}
