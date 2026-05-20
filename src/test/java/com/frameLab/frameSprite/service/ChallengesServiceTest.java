package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ChallengesServiceTest {

    private ChallengesService challengesService;
    private MockedStatic<SessionUtils> mockedSessionUtils;
    private MockedStatic<ApiUtils> mockedApiUtils;
    private SessionUtils mockCache;

    @BeforeEach
    void setUp() throws IOException {
        challengesService = new ChallengesService();

        // ARRANGE
        mockCache = mock(SessionUtils.class);
        mockedSessionUtils = mockStatic(SessionUtils.class);
        mockedSessionUtils.when(SessionUtils::getInstance).thenReturn(mockCache);

        mockedApiUtils = mockStatic(ApiUtils.class);
    }

    @AfterEach
    void tearDown() {
        mockedSessionUtils.close();
        mockedApiUtils.close();
    }


    @Test
    void shouldReturnChallengeFromCacheIfAvailable() throws Exception {
        // ARRANGE
        Challenge cachedChallenge = new Challenge();
        cachedChallenge.setTheme_title("Theme");
        when(mockCache.getChallenge()).thenReturn(cachedChallenge);

        // ACT
        Challenge result = challengesService.getCurrentChallenge();

        // ASSERT
        assertEquals("Theme", result.getTitle());
        mockedApiUtils.verify(ApiUtils::getCurrentChallenge, never());
    }

    @Test
    void shouldFetchAndCacheChallengeIfCacheIsEmpty() throws Exception {
        // ARRANGE
        when(mockCache.getChallenge()).thenReturn(null);

        Challenge apiChallenge = new Challenge();
        apiChallenge.setTheme_title("Theme");
        mockedApiUtils.when(ApiUtils::getCurrentChallenge).thenReturn(apiChallenge);

        // ACT
        Challenge result = challengesService.getCurrentChallenge();

        // ASSERT
        assertEquals("Theme", result.getTitle());
        verify(mockCache).setChallenge(apiChallenge);
    }


    @Test
    void shouldThrowExceptionIfPreviewFileDoesNotExist() {
        // ARRANGE
        Project project = new Project();
        File nonExistentFile = new File("fake/path/does_not_exist.png");

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> challengesService.uploadChallengeEntry(project, nonExistentFile)
        );
        assertEquals("No Preview image found to upload.", exception.getMessage());
    }

    @Test
    void shouldUploadSuccessfullyWhenApiReturns200() throws Exception {
        // ARRANGE
        Project project = new Project();
        project.setChallengeId(10);
        File tempFile = File.createTempFile("preview", ".png");
        tempFile.deleteOnExit();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1);
        when(mockCache.getUser()).thenReturn(mockUser);

        mockedApiUtils.when(() -> ApiUtils.uploadEntry(anyInt(), anyInt(), any(File.class))).thenReturn(200);

        // ACT & ASSERT
        assertDoesNotThrow(() -> challengesService.uploadChallengeEntry(project, tempFile));
    }

    @Test
    void shouldThrowExceptionWhenApiReturns404() throws Exception {
        // ARRANGE
        Project project = new Project();
        project.setChallengeId(10);
        File tempFile = File.createTempFile("preview", ".png");
        tempFile.deleteOnExit();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1);
        when(mockCache.getUser()).thenReturn(mockUser);

        mockedApiUtils.when(() -> ApiUtils.uploadEntry(anyInt(), anyInt(), any(File.class))).thenReturn(404);

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> challengesService.uploadChallengeEntry(project, tempFile)
        );
        assertEquals("You already have an Entry for this challenge", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenApiReturns401() throws Exception {
        // ARRANGE
        Project project = new Project();
        File tempFile = File.createTempFile("preview", ".png");
        tempFile.deleteOnExit();

        User mockUser = mock(User.class);
        when(mockCache.getUser()).thenReturn(mockUser);

        mockedApiUtils.when(() -> ApiUtils.uploadEntry(anyInt(), anyInt(), any(File.class))).thenReturn(401);

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> challengesService.uploadChallengeEntry(project, tempFile)
        );
        assertEquals("Session Expired, please reconnect", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenApiReturnsOtherErrors() throws Exception {
        // ARRANGE
        Project project = new Project();
        File tempFile = File.createTempFile("preview", ".png");
        tempFile.deleteOnExit();

        User mockUser = mock(User.class);
        when(mockCache.getUser()).thenReturn(mockUser);

        mockedApiUtils.when(() -> ApiUtils.uploadEntry(anyInt(), anyInt(), any(File.class))).thenReturn(500);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> challengesService.uploadChallengeEntry(project, tempFile)
        );
        assertEquals("Something happened, please try later.", exception.getMessage());
    }
}