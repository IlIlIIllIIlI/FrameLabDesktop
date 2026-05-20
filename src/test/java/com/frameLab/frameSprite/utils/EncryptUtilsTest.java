package com.frameLab.frameSprite.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptUtilsTest {

    @Test
    void shouldEncryptAndDecryptStringSuccessfully() {
        // ARRANGE
        String originalText = "Password123!";

        // ACT
        String encryptedText = EncryptUtils.encrypt(originalText);
        String decryptedText = EncryptUtils.decrypt(encryptedText);

        // ASSERT
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);
        assertEquals(originalText, decryptedText);
    }

    @Test
    void shouldReturnNullWhenEncryptingNull() {
        // ACT
        String result = EncryptUtils.encrypt(null);

        // ASSERT
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDecryptingNull() {
        // ACT
        String result = EncryptUtils.decrypt(null);

        // ASSERT
        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenDecryptingInvalidData() {
        // ARRANGE
        String invalidEncryptedData = "NotValid";

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> EncryptUtils.decrypt(invalidEncryptedData));
    }
}