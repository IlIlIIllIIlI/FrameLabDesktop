package com.frameLab.frameSprite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChallengeTest {
    private Challenge challenge;

    private final int ID = 1;
    private final String TITLE = "Challenge";
    private final String DESCRIPTION = "Draw";
    private final String IMAGE_URL = "http://example.com/base.png";

    @BeforeEach
    void setUp() {
        challenge = new Challenge();
    }

    @Test
    void shouldCreateEmptyChallengeWithValidParameters() {
        // ASSERT
        assertEquals(0, challenge.getId());
        assertNull(challenge.getTitle());
        assertNull(challenge.getTheme_description());
        assertNull(challenge.getImageUrl());
    }

    @Test
    void shouldSetId() {
        // ACT
        challenge.setId(ID);

        // ASSERT
        assertEquals(ID, challenge.getId());
    }

    @Test
    void shouldGetId() {
        // ARRANGE
        challenge.setId(ID);

        // ACT & ASSERT
        assertEquals(ID, challenge.getId());
    }

    @Test
    void shouldSetTitle() {
        // ACT
        challenge.setTheme_title(TITLE);

        // ASSERT
        assertEquals(TITLE, challenge.getTitle());
    }

    @Test
    void shouldGetTitle() {
        // ARRANGE
        challenge.setTheme_title(TITLE);

        // ACT & ASSERT
        assertEquals(TITLE, challenge.getTitle());
    }

    @Test
    void shouldSetDescription() {
        // ACT
        challenge.setDescription(DESCRIPTION);

        // ASSERT
        assertEquals(DESCRIPTION, challenge.getTheme_description());
    }

    @Test
    void shouldGetDescription() {
        // ARRANGE
        challenge.setDescription(DESCRIPTION);

        // ACT & ASSERT
        assertEquals(DESCRIPTION, challenge.getTheme_description());
    }

    @Test
    void shouldSetImageUrl() {
        // ACT
        challenge.setRequired_picture_url(IMAGE_URL);

        // ASSERT
        assertEquals(IMAGE_URL, challenge.getImageUrl());
    }

    @Test
    void shouldGetImageUrl() {
        // ARRANGE
        challenge.setRequired_picture_url(IMAGE_URL);

        // ACT & ASSERT
        assertEquals(IMAGE_URL, challenge.getImageUrl());
    }
}