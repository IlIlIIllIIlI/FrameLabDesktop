package com.frameLab.frameSprite.effect.filters.kernels;

public class GaussianBlurFilter extends KernelFilter{
    @Override
    protected double[][] getKernel() {
        return new double[][]{
                {1,2,1},{2,4,2},{1,2,1}
        };
    }

    @Override
    protected double getDivisor() {
        return 16.0;
    }

    @Override
    public String getName() {
        return "Gaussian Blur";
    }
}
