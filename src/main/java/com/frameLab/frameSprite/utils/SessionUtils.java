package com.frameLab.frameSprite.utils;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.cookies.CookieUtils;

import java.io.IOException;

public class SessionUtils {
    private static SessionUtils instance;
    protected User user;
    protected Challenge challenge;

    private SessionUtils(){

    }
    public User getUser() {
        return user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public static SessionUtils getInstance() throws IOException {
        if (instance == null) {
            instance = new SessionUtils();
        }
        return instance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

}
