package com.frameLab.frameSprite.model;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ProjectTest {
    private Project project;

    private final int ID = 1;
    private final String TITLE = "Project";
    private final String IMAGE_URL = "http://example.com/preview.png";
    private final int USER_ID = 9;
    private final int CHALLENGE_ID = 10;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    @Test
    void shouldCreateEmptyProjectWithValidParameters() {
        // ASSERT
        assertEquals(0, project.getId());
        assertNull(project.getTitle());
        assertNotNull(project.getLayers());
        assertTrue(project.getLayers().isEmpty());
    }

    @Test
    void shouldCreateProjectWithIdAndTitle() {
        // ACT
        Project newProject = new Project(ID, TITLE);

        // ASSERT
        assertEquals(ID, newProject.getId());
        assertEquals(TITLE, newProject.getTitle());
        assertNotNull(newProject.getLayers());
        assertTrue(newProject.getLayers().isEmpty());
    }

    @Test
    void shouldCreateProjectWithAllParameters() {
        // ACT
        Project newProject = new Project(ID, TITLE, USER_ID, CHALLENGE_ID, WIDTH, HEIGHT);

        // ASSERT
        assertEquals(ID, newProject.getId());
        assertEquals(TITLE, newProject.getTitle());
        assertEquals(USER_ID, newProject.getUserId());
        assertEquals(CHALLENGE_ID, newProject.getChallengeId());
        assertEquals(WIDTH, newProject.getWidth());
        assertEquals(HEIGHT, newProject.getHeight());
        assertNotNull(newProject.getLayers());
        assertTrue(newProject.getLayers().isEmpty());
    }


    @Test
    void shouldSetId() {
        // ACT
        project.setId(ID);

        // ASSERT
        assertEquals(ID, project.getId());
    }

    @Test
    void shouldGetId() {
        // ARRANGE
        project.setId(ID);

        // ACT & ASSERT
        assertEquals(ID, project.getId());
    }

    @Test
    void shouldSetTitle() {
        // ACT
        project.setTitle(TITLE);

        // ASSERT
        assertEquals(TITLE, project.getTitle());
    }

    @Test
    void shouldGetTitle() {
        // ARRANGE
        project.setTitle(TITLE);

        // ACT & ASSERT
        assertEquals(TITLE, project.getTitle());
    }

    @Test
    void shouldSetImageUrl() {
        // ACT
        project.setImageUrl(IMAGE_URL);

        // ASSERT
        assertEquals(IMAGE_URL, project.getImageUrl());
    }

    @Test
    void shouldGetImageUrl() {
        // ARRANGE
        project.setImageUrl(IMAGE_URL);

        // ACT & ASSERT
        assertEquals(IMAGE_URL, project.getImageUrl());
    }

    @Test
    void shouldSetUserId() {
        // ACT
        project.setUserId(USER_ID);

        // ASSERT
        assertEquals(USER_ID, project.getUserId());
    }

    @Test
    void shouldGetUserId() {
        // ARRANGE
        project.setUserId(USER_ID);

        // ACT & ASSERT
        assertEquals(USER_ID, project.getUserId());
    }

    @Test
    void shouldSetChallengeId() {
        // ACT
        project.setChallengeId(CHALLENGE_ID);

        // ASSERT
        assertEquals(CHALLENGE_ID, project.getChallengeId());
    }

    @Test
    void shouldGetChallengeId() {
        // ARRANGE
        project.setChallengeId(CHALLENGE_ID);

        // ACT & ASSERT
        assertEquals(CHALLENGE_ID, project.getChallengeId());
    }

    @Test
    void shouldSetWidth() {
        // ACT
        project.setWidth(WIDTH);

        // ASSERT
        assertEquals(WIDTH, project.getWidth());
    }

    @Test
    void shouldGetWidth() {
        // ARRANGE
        project.setWidth(WIDTH);

        // ACT & ASSERT
        assertEquals(WIDTH, project.getWidth());
    }

    @Test
    void shouldSetHeight() {
        // ACT
        project.setHeight(HEIGHT);

        // ASSERT
        assertEquals(HEIGHT, project.getHeight());
    }

    @Test
    void shouldGetHeight() {
        // ARRANGE
        project.setHeight(HEIGHT);

        // ACT & ASSERT
        assertEquals(HEIGHT, project.getHeight());
    }

    @Test
    void shouldSetLayers() {
        // ARRANGE
        List<SpriteLayer> mockLayers = new ArrayList<>();
        mockLayers.add(mock(SpriteLayer.class));
        mockLayers.add(mock(SpriteLayer.class));

        // ACT
        project.setLayers(mockLayers);

        // ASSERT
        assertEquals(2, project.getLayers().size());
        assertEquals(mockLayers, project.getLayers());
    }

    @Test
    void shouldGetLayers() {
        // ARRANGE
        List<SpriteLayer> mockLayers = new ArrayList<>();
        mockLayers.add(mock(SpriteLayer.class));
        project.setLayers(mockLayers);

        // ACT & ASSERT
        assertEquals(mockLayers, project.getLayers());
    }

    @Test
    void shouldSetLastModified() {
        // ARRANGE
        Date now = new Date();

        // ACT
        project.setLastModified(now);

        // ASSERT
        assertEquals(now, project.getLastModified());
    }

    @Test
    void shouldGetLastModified() {
        // ARRANGE
        Date now = new Date();
        project.setLastModified(now);

        // ACT & ASSERT
        assertEquals(now, project.getLastModified());
    }
}