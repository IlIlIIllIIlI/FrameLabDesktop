package com.frameLab.frameSprite.effect.filters.kernels;

public class BlurFilter extends KernelFilter {
    @Override
    protected double[][] getKernel() {
        return new double[][]{
                {1,1,1},{1,1,1},{1,1,1}
        };
    }

    @Override
    protected double getDivisor() {
        return 9.0;
    }

    @Override
    public String getName() {
        return "Blur";
    }
}
