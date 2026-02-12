package com.frameLab.frameSprite.utils.drawing;

import java.util.Objects;

public class TileKey {
    public final int x;
    public final int y;

    public TileKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileKey tileKey = (TileKey) o;
        return x == tileKey.x && y == tileKey.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
