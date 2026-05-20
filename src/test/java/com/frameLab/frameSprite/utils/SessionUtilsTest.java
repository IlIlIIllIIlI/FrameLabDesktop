package com.frameLab.frameSprite.utils;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SessionUtilsTest {

    private SessionUtils sessionUtils;

    @BeforeEach
    void setUp() throws IOException {
        SessionUtils.clearInstance();
        sessionUtils = SessionUtils.getInstance();
    }

    @AfterEach
    void tearDown() {
        SessionUtils.clearInstance();
    }

    @Test
    void shouldReturnSameInstance() throws IOException {
        // ACT
        SessionUtils anotherInstance = SessionUtils.getInstance();

        // ASSERT
        assertSame(sessionUtils, anotherInstance);
    }

    @Test
    void shouldSetAndGetUser() {
        // ARRANGE
        User user = new User();
        user.setId(1);

        // ACT
        sessionUtils.setUser(user);

        // ASSERT
        assertEquals(user, sessionUtils.getUser());
    }

    @Test
    void shouldClearInstance() throws IOException {
        // ARRANGE
        SessionUtils firstInstance = SessionUtils.getInstance();

        // ACT
        SessionUtils.clearInstance();
        SessionUtils secondInstance = SessionUtils.getInstance();

        // ASSERT
        assertNotSame(firstInstance, secondInstance);
    }
}