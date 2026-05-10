package com.frameLab.frameSprite.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameLab.frameSprite.dto.ApiResponse;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.cookies.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.net.CookiePolicy.ACCEPT_ALL;

public class ApiUtils {
    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);
    static HttpClient client;
    static CookieUtils cu;
    static String apiUrl;
    static ObjectMapper objectMapper;
    private ApiUtils() {


    }

    static {
        try {
            cu = CookieUtils.getInstance();

            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Properties config = new Properties();
            config.load(ApiUtils.class.getResourceAsStream("/config.properties"));
            apiUrl = config.getProperty("apiUrl");
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .cookieHandler(new CookieManager(cu.getCookieStore(),ACCEPT_ALL))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLogged() {
        return cu.getSession() != null;
    }

    

    private static <T> T execute(HttpRequest request, Class<T> responseType) throws Exception {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, responseType);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        try {
            ApiResponse<T> apiResponse = objectMapper.readValue(response.body(), type);

            if (response.statusCode() != 200) {
                String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Error: " + response.statusCode();
                throw new ApiException(errorMsg);
            }


            return apiResponse.getData();


        } catch (JsonProcessingException e){
            if (response.statusCode() != 200) {
                throw new ApiException("Server Error: " + response.statusCode());
            }

            if (responseType == String.class) {
                return responseType.cast(response.body());
            }

            throw new Exception("Failed to parse API response", e);
        }


    }

    private static <T> T getObject(String endpoint, Class<T> returnType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + endpoint))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return execute(request, returnType);
    }




    public static String getFirstName() throws Exception {
        return getObject("/auth/me", User.class).getFirstName();

    }

    public static User getMe() throws Exception {
        return getObject("/auth/me", User.class);

    }

    public static void logOut() throws Exception {
        getObject("/auth/logout/", String.class);
    }

    public static void login(String email, String password) throws Exception {
        Map<String,String> data = new HashMap<>();
        data.put("email",email);
        data.put("password",password);
        String jsonBody = objectMapper.writeValueAsString(data);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+"/auth/login"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            execute(request, String.class);

    }

    public static Challenge getCurrentChallenge() throws Exception {
            return getObject("/challenges/current", Challenge.class);
    }


    public static int uploadEntry(int userId, int challengeId, File imageFile) throws IOException, InterruptedException {
        String boundary = "---Boundary" + System.currentTimeMillis();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteStream, StandardCharsets.UTF_8), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"userId\"\r\n\r\n");
        writer.append(String.valueOf(userId)).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"challengeId\"\r\n\r\n");
        writer.append(String.valueOf(challengeId)).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"picture\"; filename=\"").append(imageFile.getName()).append("\"\r\n");
        writer.append("Content-Type: image/png\r\n\r\n");
        writer.flush();

        Files.copy(imageFile.toPath(), byteStream);
        byteStream.flush();

        writer.append("\r\n--").append(boundary).append("--\r\n");
        writer.flush();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/entries"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        return response.statusCode();
    }
}
