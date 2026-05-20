package com.frameLab.frameSprite.effect.filters.pixels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SepiaFilterTest {

    private SepiaFilter sepiaFilter;

    @BeforeEach
    void setUp() {
        sepiaFilter = new SepiaFilter();
    }

    @Test
    void shouldReturnCorrectName() {
        // ACT
        String name = sepiaFilter.getName();

        // ASSERT
        assertEquals("Sepia", name);
    }

    @Test
    void shouldReturnSamePixelIfTransparent() {
        // ARRANGE
        int transparentPixel = 0x00FFFFFF; // Alpha is 00

        // ACT
        int result = sepiaFilter.processPixel(transparentPixel);

        // ASSERT
        assertEquals(transparentPixel, result);
    }

    @Test
    void shouldProcessPureBlackCorrectly() {
        // ARRANGE
        int pureBlack = 0xFF000000; // A=255, R=0, G=0, B=0

        // ACT
        int result = sepiaFilter.processPixel(pureBlack);

        // ASSERT
        assertEquals(pureBlack, result);
    }

    @Test
    void shouldApplySepiaMathToPureRed() {
        // ARRANGE
        int pureRed = 0xFFFF0000; // A=255, R=255, G=0, B=0

        // Expected Math for Red=255, G=0, B=0:
        // finalRed = 255 * 0.393 = 100.215 -> 100
        // finalGreen = 255 * 0.349 = 88.995 -> 88
        // finalBlue = 255 * 0.272 = 69.36 -> 69
        int expectedSepia = (255 << 24) | (100 << 16) | (88 << 8) | 69;

        // ACT
        int result = sepiaFilter.processPixel(pureRed);

        // ASSERT
        assertEquals(expectedSepia, result);
    }

    @Test
    void shouldCapValuesAt255ForPureWhite() {
        // ARRANGE
        int pureWhite = 0xFFFFFFFF; // A=255, R=255, G=255, B=255

        // Expected Math for White:
        // finalRed = min(344.5, 255) = 255
        // finalGreen = min(306.765, 255) = 255
        // finalBlue = min(238.935, 255) = 238
        int expectedSepia = (255 << 24) | (255 << 16) | (255 << 8) | 238;

        // ACT
        int result = sepiaFilter.processPixel(pureWhite);

        // ASSERT
        assertEquals(expectedSepia, result);
    }
}