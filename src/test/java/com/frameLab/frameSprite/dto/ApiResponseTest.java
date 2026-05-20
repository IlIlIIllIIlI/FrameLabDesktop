package com.frameLab.frameSprite.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseTest {

    private ApiResponse<String> apiResponse;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        apiResponse = new ApiResponse<>();
        mapper = new ObjectMapper();
    }

    @Test
    void shouldSetAndGetSuccess() {
        // ACT
        apiResponse.setSuccess(true);

        // ASSERT
        assertTrue(apiResponse.isSuccess());
    }

    @Test
    void shouldSetAndGetMessage() {
        // ACT
        apiResponse.setMessage("Operation successful");

        // ASSERT
        assertEquals("Operation successful", apiResponse.getMessage());
    }

    @Test
    void shouldSetAndGetData() {
        // ACT
        apiResponse.setData("Test Data");

        // ASSERT
        assertEquals("Test Data", apiResponse.getData());
    }


    @Test
    void shouldDeserializeUsingDataAlias() throws JsonProcessingException {
        // ARRANGE
        String json = "{\"success\":true, \"message\":\"OK\", \"data\":\"value_data\"}";

        // ACT
        ApiResponse<String> response = mapper.readValue(json, new TypeReference<ApiResponse<String>>() {});

        // ASSERT
        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals("value_data", response.getData());
    }

    @Test
    void shouldDeserializeUsingUserAlias() throws JsonProcessingException {
        // ARRANGE
        String json = "{\"success\":true, \"message\":\"OK\", \"user\":\"user_data\"}";

        // ACT
        ApiResponse<String> response = mapper.readValue(json, new TypeReference<ApiResponse<String>>() {});

        // ASSERT
        assertEquals("user_data", response.getData());
    }

    @Test
    void shouldDeserializeUsingChallengeAlias() throws JsonProcessingException {
        // ARRANGE
        String json = "{\"success\":true, \"message\":\"OK\", \"challenge\":\"challenge_data\"}";

        // ACT
        ApiResponse<String> response = mapper.readValue(json, new TypeReference<ApiResponse<String>>() {});

        // ASSERT
        assertEquals("challenge_data", response.getData());
    }
}