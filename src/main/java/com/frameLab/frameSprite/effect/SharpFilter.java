package com.frameLab.frameSprite.effect;

public class SharpFilter extends KernelFilter{

    @Override
    protected double[][] getKernel() {
        return new double[][]{
                {0,-1,0},{-1,5,-1},{0,-1,0}
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
