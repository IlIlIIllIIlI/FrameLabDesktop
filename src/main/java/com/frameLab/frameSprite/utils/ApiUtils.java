package com.frameLab.frameSprite.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameLab.frameSprite.utils.cookies.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.frameLab.frameSprite.utils.JsonUtils.mapToJson;
import static java.net.CookiePolicy.ACCEPT_ALL;

public class ApiUtils {
    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);
    static HttpClient client;
    static CookieUtils cu;
    static String apiUrl;
    ObjectMapper objectMapper;
    public ApiUtils() throws IOException {
         cu = CookieUtils.getInstance();

        objectMapper = new ObjectMapper();

        Properties config = new Properties();
        config.load(getClass().getResourceAsStream("/config.properties"));
        apiUrl = config.getProperty("apiUrl");
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(new CookieManager(cu.getCookieStore(),ACCEPT_ALL))
                .build();

    }

    public boolean isLogged() {
        return cu.getSession() != null;
    }

    public String getEmail() throws LoginException {

        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+"/auth/me"))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.readValue(response.body(), JsonUtils.Parsing.class).getUser().getEmail();
            }
            throw new Exception("API ERROR : " + response.body());
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

    public void logOut() throws Exception {
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+"/auth/logout/"))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                return;
            }
            throw new Exception("API ERROR : " + response.statusCode());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static boolean  login(String email, String password) throws LoginException {
        Map<String,String> data = new HashMap<>();
        data.put("email",email);
        data.put("password",password);
        String jsonBody = mapToJson(data);
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+"/auth/login"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                return true;
            }

            throw new Exception("Api Error" + response.statusCode());

        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

    public Object getObject(String url) throws Exception {
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.readValue(response.body(), JsonUtils.Parsing.class).getMainClass();
            }
            throw new Exception("API ERROR : " + response.statusCode());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public int uploadEntry(int userId, int challengeId, File imageFile) throws Exception {
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
