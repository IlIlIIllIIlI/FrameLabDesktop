package com.frameLab.frameSprite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {
    private User user;
    private final int ID = 1;
    private final String FIRST_NAME = "test";
    private final String LAST_NAME = "Test";
    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void shouldCreateEmptyUserWithValidParameters() {
        // ASSERT
        assertEquals(0, user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getProjects());
    }


    @Test
    void shouldSetId() {
        // ACT
        user.setId(ID);

        // ASSERT
        assertEquals(ID, user.getId());
    }

    @Test
    void shouldGetId() {
        // ARRANGE
        user.setId(ID);

        // ACT & ASSERT
        assertEquals(ID, user.getId());
    }

    @Test
    void shouldSetFirstName() {
        // ACT
        user.setFirst_name(FIRST_NAME);

        // ASSERT
        assertEquals(FIRST_NAME, user.getFirstName());
    }

    @Test
    void shouldGetFirstName() {
        // ARRANGE
        user.setFirst_name(FIRST_NAME);

        // ACT & ASSERT
        assertEquals(FIRST_NAME, user.getFirstName());
    }

    @Test
    void shouldSetLastName() {
        // ACT
        user.setLast_name(LAST_NAME);

        // ASSERT
        assertEquals(LAST_NAME, user.getLastName());
    }

    @Test
    void shouldGetLastName() {
        // ARRANGE
        user.setLast_name(LAST_NAME);

        // ACT & ASSERT
        assertEquals(LAST_NAME, user.getLastName());
    }

    @Test
    void shouldSetEmail() {
        // ACT
        user.setEmail(EMAIL);

        // ASSERT
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void shouldGetEmail() {
        // ARRANGE
        user.setEmail(EMAIL);

        // ACT & ASSERT
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void shouldSetProjects() {
        // ARRANGE
        List<Project> projectList = new ArrayList<>();

        // ACT
        user.setProjects(projectList);

        // ASSERT
        assertEquals(projectList, user.getProjects());
    }

    @Test
    void shouldGetProjects() {
        // ARRANGE
        List<Project> projectList = new ArrayList<>();
        user.setProjects(projectList);

        // ACT & ASSERT
        assertEquals(projectList, user.getProjects());
    }
}