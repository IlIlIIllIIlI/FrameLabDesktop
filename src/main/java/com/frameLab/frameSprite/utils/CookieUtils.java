package com.frameLab.frameSprite.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class CookieUtils {
    private static CookieUtils instance;
    CookieManager cm;
    CookieStore cs;
    String uri;
    private CookieUtils() throws IOException {
        cm = new CookieManager();
        cs = cm.getCookieStore();
        Properties config = new Properties();
        config.load(new FileInputStream("config.properties"));
        uri = config.getProperty("apiUrl");
    }

    public HttpCookie getSession(){
        System.out.println(cs.getCookies());
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
