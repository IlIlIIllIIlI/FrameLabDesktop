package com.frameLab.frameSprite.utils.cookies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.net.HttpCookie;

import static org.junit.jupiter.api.Assertions.*;

public class CookieParsingTest {

    private CookieParsing cookieParsing;

    @BeforeEach
    void setUp() {
        cookieParsing = new CookieParsing();
    }

    @Test
    void shouldCreateEmptyCookieParsingAndSetGetValues() {
        // ARRANGE & ACT
        cookieParsing.setName("session_id");
        cookieParsing.setValue("12345");
        cookieParsing.setComment("test comment");
        cookieParsing.setCommentURL("http://comment.url");
        cookieParsing.setToDiscard(true);
        cookieParsing.setDomain("example.com");
        cookieParsing.setMaxAge(3600L);
        cookieParsing.setPath("/path");
        cookieParsing.setPortlist("80,443");
        cookieParsing.setSecure(true);
        cookieParsing.setHttpOnly(true);
        cookieParsing.setVersion(1);

        // ASSERT
        assertEquals("session_id", cookieParsing.getName());
        assertEquals("12345", cookieParsing.getValue());
        assertEquals("test comment", cookieParsing.getComment());
        assertEquals("http://comment.url", cookieParsing.getCommentURL());
        assertTrue(cookieParsing.isToDiscard());
        assertEquals("example.com", cookieParsing.getDomain());
        assertEquals(3600L, cookieParsing.getMaxAge());
        assertEquals("/path", cookieParsing.getPath());
        assertEquals("80,443", cookieParsing.getPortlist());
        assertTrue(cookieParsing.isSecure());
        assertTrue(cookieParsing.isHttpOnly());
        assertEquals(1, cookieParsing.getVersion());
    }

    @Test
    void shouldCreateFromHttpCookie() {
        // ARRANGE
        CookieParsing parsedCookie = getCookieParsing();

        // ASSERT
        assertEquals("auth_token", parsedCookie.getName());
        assertEquals("abcde", parsedCookie.getValue());
        assertEquals("auth comment", parsedCookie.getComment());
        
        assertEquals("http://auth.url", parsedCookie.getCommentURL());

        assertFalse(parsedCookie.isToDiscard());
        assertEquals("test.com", parsedCookie.getDomain());
        assertEquals(7200L, parsedCookie.getMaxAge());
        assertEquals("/api", parsedCookie.getPath());
        assertEquals("8080", parsedCookie.getPortlist());
        assertFalse(parsedCookie.isSecure());
        assertFalse(parsedCookie.isHttpOnly());
        assertEquals(0, parsedCookie.getVersion());
    }

    @Nonnull
    private static CookieParsing getCookieParsing() {
        HttpCookie httpCookie = new HttpCookie("auth_token", "abcde");
        httpCookie.setComment("auth comment");
        httpCookie.setCommentURL("http://auth.url");
        httpCookie.setDiscard(false);
        httpCookie.setDomain("test.com");
        httpCookie.setMaxAge(7200L);
        httpCookie.setPath("/api");
        httpCookie.setPortlist("8080");
        httpCookie.setSecure(false);
        httpCookie.setHttpOnly(false);
        httpCookie.setVersion(0);

        // ACT
        return new CookieParsing(httpCookie);
    }

    @Test
    void shouldConvertToHttpCookie() {
        // ARRANGE
        cookieParsing.setName("my_cookie");
        cookieParsing.setValue("my_value");
        cookieParsing.setComment("my comment");
        cookieParsing.setCommentURL("http://my.url");
        cookieParsing.setToDiscard(true);
        cookieParsing.setDomain("mydomain.com");
        cookieParsing.setMaxAge(1000L);
        cookieParsing.setPath("/my-path");
        cookieParsing.setPortlist("80");
        cookieParsing.setSecure(true);
        cookieParsing.setHttpOnly(true);
        cookieParsing.setVersion(1);

        // ACT
        HttpCookie resultCookie = cookieParsing.toCookie();

        // ASSERT
        assertEquals("my_cookie", resultCookie.getName());
        assertEquals("my_value", resultCookie.getValue());
        assertEquals("my comment", resultCookie.getComment());
        assertEquals("http://my.url", resultCookie.getCommentURL());
        assertTrue(resultCookie.getDiscard());
        assertEquals("mydomain.com", resultCookie.getDomain());
        assertEquals(1000L, resultCookie.getMaxAge());
        assertEquals("/my-path", resultCookie.getPath());
        assertEquals("80", resultCookie.getPortlist());
        assertTrue(resultCookie.getSecure());
        assertTrue(resultCookie.isHttpOnly());
        assertEquals(1, resultCookie.getVersion());
    }
}