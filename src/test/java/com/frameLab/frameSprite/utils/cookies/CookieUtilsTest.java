package com.frameLab.frameSprite.utils.cookies;

import com.frameLab.frameSprite.utils.EncryptUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class CookieUtilsTest {

    private MockedStatic<EncryptUtils> mockedEncryptUtils;
    private MockedStatic<Files> mockedFiles;

    @BeforeEach
    void setUp() throws Exception {
        // ARRANGE
        resetSingleton();

        mockedEncryptUtils = mockStatic(EncryptUtils.class);
        mockedFiles = mockStatic(Files.class);

        mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedEncryptUtils.close();
        mockedFiles.close();
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        Field instanceField = CookieUtils.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void shouldSaveCookiesEncrypted() throws Exception {
        // ARRANGE
        CookieUtils cookieUtils = CookieUtils.getInstance();
        HttpCookie myCookie = new HttpCookie("testCookie", "testValue");
        cookieUtils.getCookieStore().add(URI.create("http://localhost:8000"), myCookie);

        mockedEncryptUtils.when(() -> EncryptUtils.encrypt(any(String.class))).thenReturn("encrypted_payload");

        // ACT
        cookieUtils.save();

        // ASSERT
        mockedEncryptUtils.verify(() -> EncryptUtils.encrypt(Mockito.contains("testCookie")));
        mockedFiles.verify(() -> Files.writeString(eq(Path.of("cookies.enc")), eq("encrypted_payload")));
    }

    @Test
    void shouldLoadAndDecryptCookies() throws Exception {
        // ARRANGE
        mockedFiles.when(() -> Files.exists(eq(Path.of("cookies.enc")))).thenReturn(true);
        mockedFiles.when(() -> Files.readString(eq(Path.of("cookies.enc")))).thenReturn("encrypted_data");

        String jsonCookies = "[{\"name\":\"session\",\"value\":\"abc1234\",\"domain\":\"localhost.local\",\"maxAge\":3600}]";
        mockedEncryptUtils.when(() -> EncryptUtils.decrypt("encrypted_data")).thenReturn(jsonCookies);

        // ACT
        CookieUtils cookieUtils = CookieUtils.getInstance();
        cookieUtils.loadCookies();

        // ASSERT
        assertEquals(1, cookieUtils.getCookieStore().getCookies().size());
        assertEquals("session", cookieUtils.getCookieStore().getCookies().getFirst().getName());
        assertEquals("abc1234", cookieUtils.getCookieStore().getCookies().getFirst().getValue());
    }

    @Test
    void shouldReturnSessionCookie() throws Exception {
        // ARRANGE
        CookieUtils cookieUtils = CookieUtils.getInstance();

        Field uriField = CookieUtils.class.getDeclaredField("uri");
        uriField.setAccessible(true);
        uriField.set(cookieUtils, "http://myapi.com");

        HttpCookie sessionCookie = new HttpCookie("session", "my-session-token");
        cookieUtils.getCookieStore().add(URI.create("http://myapi.com"), sessionCookie);

        HttpCookie otherCookie = new HttpCookie("other", "value");
        cookieUtils.getCookieStore().add(URI.create("http://myapi.com"), otherCookie);

        // ACT
        HttpCookie result = cookieUtils.getSession();

        // ASSERT
        assertNotNull(result);
        assertEquals("session", result.getName());
        assertEquals("my-session-token", result.getValue());
    }

    @Test
    void shouldReturnNullIfNoSessionCookie() throws Exception {
        // ARRANGE
        CookieUtils cookieUtils = CookieUtils.getInstance();

        Field uriField = CookieUtils.class.getDeclaredField("uri");
        uriField.setAccessible(true);
        uriField.set(cookieUtils, "http://myapi.com");

        HttpCookie otherCookie = new HttpCookie("other", "value");
        cookieUtils.getCookieStore().add(URI.create("http://myapi.com"), otherCookie);

        // ACT
        HttpCookie result = cookieUtils.getSession();

        // ASSERT
        assertNull(result);
    }
}