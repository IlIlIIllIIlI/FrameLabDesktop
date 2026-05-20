package com.frameLab.frameSprite.dto;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpriteLayerDTOTest {

    private SpriteLayerDTO dto;

    @BeforeEach
    void setUp() {
        dto = new SpriteLayerDTO();
    }

    @Test
    void shouldCreateEmptyDTO() {
        // ASSERT
        assertNull(dto.getName());
        assertFalse(dto.isVisible());
        assertEquals(0.0, dto.getOpacity());
        assertEquals(0, dto.getX());
        assertEquals(0, dto.getY());
        assertNull(dto.getImageFileName());
    }

    @Test
    void shouldCreateDTOFromSpriteLayer() {
        // ARRANGE
        SpriteLayer mockLayer = mock(SpriteLayer.class);
        when(mockLayer.getName()).thenReturn("TestLayer");
        when(mockLayer.isVisible()).thenReturn(true);
        when(mockLayer.getOpacity()).thenReturn(0.8);
        when(mockLayer.getX()).thenReturn(15);
        when(mockLayer.getY()).thenReturn(25);
        when(mockLayer.getImageFileName()).thenReturn("layer.png");

        // ACT
        SpriteLayerDTO mappedDto = new SpriteLayerDTO(mockLayer);

        // ASSERT
        assertEquals("TestLayer", mappedDto.getName());
        assertTrue(mappedDto.isVisible());
        assertEquals(0.8, mappedDto.getOpacity());
        assertEquals(15, mappedDto.getX());
        assertEquals(25, mappedDto.getY());
        assertEquals("layer.png", mappedDto.getImageFileName());
    }

    @Test
    void shouldConvertDTOToSpriteLayer() {
        // ARRANGE
        dto.setName("ConvertedLayer");
        dto.setVisible(true);
        dto.setOpacity(0.5);
        dto.setX(100);
        dto.setY(200);
        dto.setImageFileName("converted.png");

        // ACT
        SpriteLayer layer = dto.toLayer();

        // ASSERT
        assertEquals("ConvertedLayer", layer.getName());
        assertTrue(layer.isVisible());
        assertEquals(0.5, layer.getOpacity());
        assertEquals(100, layer.getX());
        assertEquals(200, layer.getY());
        assertEquals("converted.png", layer.getImageFileName());
    }

    @Test
    void shouldSetAndGetName() {
        // ACT
        dto.setName("MyLayer");

        // ASSERT
        assertEquals("MyLayer", dto.getName());
    }

    @Test
    void shouldSetAndGetVisible() {
        // ACT
        dto.setVisible(true);

        // ASSERT
        assertTrue(dto.isVisible());
    }

    @Test
    void shouldSetAndGetOpacity() {
        // ACT
        dto.setOpacity(0.75);

        // ASSERT
        assertEquals(0.75, dto.getOpacity());
    }

    @Test
    void shouldSetAndGetX() {
        // ACT
        dto.setX(50);

        // ASSERT
        assertEquals(50, dto.getX());
    }

    @Test
    void shouldSetAndGetY() {
        // ACT
        dto.setY(60);

        // ASSERT
        assertEquals(60, dto.getY());
    }

    @Test
    void shouldSetAndGetImageFileName() {
        // ACT
        dto.setImageFileName("file.png");

        // ASSERT
        assertEquals("file.png", dto.getImageFileName());
    }
}