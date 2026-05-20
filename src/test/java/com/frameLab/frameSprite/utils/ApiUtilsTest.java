package com.frameLab.frameSprite.utils;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.cookies.CookieUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApiUtilsTest {

    private HttpClient mockClient;
    private HttpResponse<String> mockResponse;
    private CookieUtils mockCookieUtils;

    private HttpClient originalClient;
    private CookieUtils originalCookieUtils;

    private MockedStatic<Files> mockedFiles;

    @BeforeEach
    void setUp() throws Exception {
        // ARRANGE

        mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
        mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

        mockClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
        mockCookieUtils = mock(CookieUtils.class);

        Field clientField = ApiUtils.class.getDeclaredField("client");
        clientField.setAccessible(true);
        originalClient = (HttpClient) clientField.get(null);
        clientField.set(null, mockClient);

        Field cuField = ApiUtils.class.getDeclaredField("cu");
        cuField.setAccessible(true);
        originalCookieUtils = (CookieUtils) cuField.get(null);
        cuField.set(null, mockCookieUtils);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field clientField = ApiUtils.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(null, originalClient);

        Field cuField = ApiUtils.class.getDeclaredField("cu");
        cuField.setAccessible(true);
        cuField.set(null, originalCookieUtils);

        mockedFiles.close();
    }

    @Test
    void shouldReturnTrueWhenLogged() {
        // ARRANGE
        when(mockCookieUtils.getSession()).thenReturn(new HttpCookie("session", "token"));

        // ACT
        boolean isLogged = ApiUtils.isLogged();

        // ASSERT
        assertTrue(isLogged);
    }

    @Test
    void shouldReturnFalseWhenNotLogged() {
        // ARRANGE
        when(mockCookieUtils.getSession()).thenReturn(null);

        // ACT
        boolean isLogged = ApiUtils.isLogged();

        // ASSERT
        assertFalse(isLogged);
    }

    @Test
    void shouldGetMeSuccessfully() throws Exception {
        // ARRANGE
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"success\":true, \"data\":{\"id\":1, \"firstName\":\"John\"}}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // ACT
        User result = ApiUtils.getMe();

        // ASSERT
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void shouldThrowExceptionWhenGetMeFails() throws Exception {
        // ARRANGE
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"success\":false, \"message\":\"Unauthorized\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // ACT & ASSERT
        Exception exception = assertThrows(Exception.class, ApiUtils::getMe);
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // ARRANGE
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"success\":true, \"data\":\"Login Success\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // ACT & ASSERT
        assertDoesNotThrow(() -> ApiUtils.login("test@test.com", "password123"));
    }

    @Test
    void shouldGetCurrentChallengeSuccessfully() throws Exception {
        // ARRANGE
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"success\":true, \"data\":{\"id\":5, \"title\":\"Test Challenge\"}}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // ACT
        Challenge result = ApiUtils.getCurrentChallenge();

        // ASSERT
        assertNotNull(result);
        assertEquals(5, result.getId());
    }

    @Test
    void shouldUploadEntrySuccessfully() throws Exception {
        // ARRANGE
        File tempFile = File.createTempFile("testImage", ".png");
        tempFile.deleteOnExit();

        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("Created");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // ACT
        int statusCode = ApiUtils.uploadEntry(1, 2, tempFile);

        // ASSERT
        assertEquals(201, statusCode);
        verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}