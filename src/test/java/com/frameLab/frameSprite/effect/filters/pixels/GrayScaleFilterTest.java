package com.frameLab.frameSprite.effect.filters.pixels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrayScaleFilterTest {

    private GrayScaleFilter grayScaleFilter;

    @BeforeEach
    void setUp() {
        grayScaleFilter = new GrayScaleFilter();
    }

    @Test
    void shouldReturnCorrectName() {
        // ACT
        String name = grayScaleFilter.getName();

        // ASSERT
        assertEquals("Black & White", name);
    }

    @Test
    void shouldReturnSamePixelIfTransparent() {
        // ARRANGE
        int transparentPixel = 0x00998877; // Alpha is 00, RGB doesn't matter

        // ACT
        int result = grayScaleFilter.processPixel(transparentPixel);

        // ASSERT
        assertEquals(transparentPixel, result);
    }

    @Test
    void shouldProcessPureBlackCorrectly() {
        // ARRANGE
        int pureBlack = 0xFF000000; // A=255, R=0, G=0, B=0

        // ACT
        int result = grayScaleFilter.processPixel(pureBlack);

        // ASSERT
        assertEquals(pureBlack, result); // Luminance of 0 is 0
    }

    @Test
    void shouldProcessPureWhiteCorrectly() {
        // ARRANGE
        int pureWhite = 0xFFFFFFFF; // A=255, R=255, G=255, B=255

        // Expected Lum = 255*0.2126 + 255*0.7152 + 255*0.0722 = 255
        int expectedGray = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        // ACT
        int result = grayScaleFilter.processPixel(pureWhite);

        // ASSERT
        assertEquals(expectedGray, result);
    }

    @Test
    void shouldApplyLuminanceFormulaToPureRed() {
        // ARRANGE
        int pureRed = 0xFFFF0000; // A=255, R=255, G=0, B=0

        // Expected Math for Red=255, G=0, B=0:
        // lum = 255 * 0.2126 = 54.213 -> 54
        int expectedGray = (255 << 24) | (54 << 16) | (54 << 8) | 54;

        // ACT
        int result = grayScaleFilter.processPixel(pureRed);

        // ASSERT
        assertEquals(expectedGray, result);
    }
}