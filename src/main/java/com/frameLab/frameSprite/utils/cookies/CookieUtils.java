package com.frameLab.frameSprite.utils.cookies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameLab.frameSprite.utils.EncryptUtils;
import com.frameLab.frameSprite.utils.JsonUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class CookieUtils extends CookieManager implements Serializable {
    private static CookieUtils instance;
    CookieManager cm;
    CookieStore cs;
    String uri;
    private CookieUtils() throws IOException {
        cm = new CookieManager();
        cs = cm.getCookieStore();
        Properties config = new Properties();
        config.load(new FileInputStream("config.properties"));
        uri = config.getProperty("uri");

        loadCookies();

        Thread savingCookies = new Thread(this::save) ;
        Runtime.getRuntime().addShutdownHook(savingCookies);
    }

    public void save() {
        try {

            ObjectMapper mapper = new ObjectMapper();
            List<HttpCookie> cookies = cs.getCookies();
            List<CookieParsing> cookieToParse = new ArrayList<>();
            for (HttpCookie cookie : cookies){

                cookieToParse.add(new CookieParsing(cookie));

            }

            String cookieJson = mapper.writeValueAsString(cookieToParse);

            String encryptedCookies = EncryptUtils.encrypt(cookieJson);
            Files.writeString(Path.of("cookies.enc"),encryptedCookies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void loadCookies() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Path path = Path.of("cookies.enc");
        if (!Files.exists(path)) {
            return;
        }

        String encryptedCookies = Files.readString(path);

        String cookies = EncryptUtils.decrypt(encryptedCookies);

        if (cookies == null ||cookies.isBlank()) {
            return;
        }
        List<CookieParsing> jsonToCookie = mapper.readValue(cookies, new TypeReference<List<CookieParsing>>() {});

        if (jsonToCookie != null) {
            for (CookieParsing cookie: jsonToCookie){
                java.net.HttpCookie cookieParsed = cookie.toCookie();
                if (Objects.equals(cookieParsed.getDomain(), "localhost.local")){
                    cs.add(URI.create("http://localhost:8000"),cookie.toCookie());
                }else{
                    cs.add(null,cookie.toCookie());
                }
            }
        }
    }
    public HttpCookie getSession(){
        List<HttpCookie> cookies = cs.get(URI.create(uri));

        for (HttpCookie cookie : cookies){
            if (Objects.equals(cookie.getName(), "session")) {
                return  cookie;
            }
        }

        return null;
    }

    public CookieStore getCookieStore() {
        return cs;
    }


    public static CookieUtils getInstance() throws IOException {
        if (instance == null) {
            instance = new CookieUtils();
        }
        return instance;
    }
}
