package com.frameLab.frameSprite.effect.filters.pixels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContrastFilterTest {

    @Test
    void shouldReturnCorrectName() {
        // ARRANGE
        ContrastFilter filter = new ContrastFilter(50);

        // ACT
        String name = filter.getName();

        // ASSERT
        assertEquals("Contrast", name);
    }

    @Test
    void shouldReturnSamePixelIfTransparent() {
        // ARRANGE
        ContrastFilter filter = new ContrastFilter(100);
        int transparentPixel = 0x00FF00FF; // Alpha is 00

        // ACT
        int result = filter.processPixel(transparentPixel);

        // ASSERT
        assertEquals(transparentPixel, result);
    }

    @Test
    void shouldNotChangeColorsWhenIntensityIsZero() {
        // ARRANGE
        ContrastFilter filter = new ContrastFilter(0);
        int originalPixel = 0xFF64A0C8; // A=255, R=100, G=160, B=200

        // ACT
        int result = filter.processPixel(originalPixel);

        // ASSERT
        assertEquals(originalPixel, result);
    }

    @Test
    void shouldPushColorsToExtremesWhenIntensityIsHigh() {
        // ARRANGE
        ContrastFilter filter = new ContrastFilter(255);

        int inputPixel = 0xFF827E80; // A=255, R=130 (>128), G=126 (<128), B=128


        int expectedPixel = (255 << 24) | (255 << 16) | (0 << 8) | 128;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }

    @Test
    void shouldPullColorsTowardsGrayWhenIntensityIsNegative() {
        // ARRANGE
        ContrastFilter filter = new ContrastFilter(-100);

        int inputPixel = 0xFF00FF80; // A=255, R=0, G=255, B=128


        double factor = (259.0 * (-100 + 255.0)) / (255.0 * (259.0 - (-100))); // ~0.4385

        int expectedRed = Math.clamp((int)(factor * (-128) + 128), 0, 255);     // ~71
        int expectedGreen = Math.clamp((int)(factor * (255 - 128) + 128), 0, 255); // ~183
        int expectedBlue = Math.clamp((int)(factor * (0) + 128), 0, 255);  // 128

        int expectedPixel = (255 << 24) | (expectedRed << 16) | (expectedGreen << 8) | expectedBlue;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }
}