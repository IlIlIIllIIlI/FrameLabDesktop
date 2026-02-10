package com.frameLab.frameSprite.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.frameLab.frameSprite.utils.JsonUtils.mapToJson;
import static java.net.CookiePolicy.ACCEPT_ALL;

public class ApiUtils {
    HttpClient client;
    CookieUtils cu;
    String apiUrl;
    ObjectMapper objectMapper;
    public ApiUtils() throws IOException {
         cu = CookieUtils.getInstance();

        objectMapper = new ObjectMapper();

        Properties config = new Properties();
        config.load(new FileInputStream("config.properties"));
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

    public boolean login(String email, String password) throws LoginException {
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
}
