package com.frameLab.frameSprite.effect;

public class EdgeFilter extends  DoubleKernelFilter {
    @Override
    protected double[][] getKernelX() {
        return new double[][]{
                {-1,0,1},{-2,0,2},{-1,0,1}
        };
    }

    @Override
    protected double[][] getKernelY() {
        return new double[][]{
                {-1,-2,-1},{0,0,0},{1,2,1}
        };
    }

    @Override
    public String getName() {
        return "Edge";
    }
}
