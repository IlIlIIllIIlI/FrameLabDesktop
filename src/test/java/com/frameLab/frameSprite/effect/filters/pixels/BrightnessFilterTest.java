package com.frameLab.frameSprite.effect.filters.pixels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrightnessFilterTest {

    @Test
    void shouldReturnCorrectName() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(50);

        // ACT
        String name = filter.getName();

        // ASSERT
        assertEquals("Brightness", name);
    }

    @Test
    void shouldReturnSamePixelIfTransparent() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(100);
        int transparentPixel = 0x00123456; // Alpha is 00

        // ACT
        int result = filter.processPixel(transparentPixel);

        // ASSERT
        assertEquals(transparentPixel, result);
    }

    @Test
    void shouldNotChangeColorsWhenIntensityIsZero() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(0);
        int originalPixel = 0xFF64A0C8; // A=255, R=100, G=160, B=200

        // ACT
        int result = filter.processPixel(originalPixel);

        // ASSERT
        assertEquals(originalPixel, result);
    }

    @Test
    void shouldIncreaseBrightnessCorrectly() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(50);
        int inputPixel = 0xFF326496; // A=255, R=50, G=100, B=150

        // R = 50+50=100, G = 100+50=150, B = 150+50=200
        int expectedPixel = (255 << 24) | (100 << 16) | (150 << 8) | 200;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }

    @Test
    void shouldDecreaseBrightnessCorrectly() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(-50);
        int inputPixel = 0xFF6496C8; // A=255, R=100, G=150, B=200

        // R = 100-50=50, G = 150-50=100, B = 200-50=150
        int expectedPixel = (255 << 24) | (50 << 16) | (100 << 8) | 150;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }

    @Test
    void shouldClampAt255WhenBrightnessIsTooHigh() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(200);
        int inputPixel = 0xFF64C8FA; // A=255, R=100, G=200, B=250

        int expectedPixel = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }

    @Test
    void shouldClampAtZeroWhenBrightnessIsTooLow() {
        // ARRANGE
        BrightnessFilter filter = new BrightnessFilter(-200);
        int inputPixel = 0xFF643214; // A=255, R=100, G=50, B=20


        int expectedPixel = (255 << 24) | (0 << 16) | (0 << 8) | 0;

        // ACT
        int result = filter.processPixel(inputPixel);

        // ASSERT
        assertEquals(expectedPixel, result);
    }
}