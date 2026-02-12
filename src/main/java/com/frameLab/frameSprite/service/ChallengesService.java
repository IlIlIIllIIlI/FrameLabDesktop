package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;

import java.io.IOException;

public class ChallengesService {
    ApiUtils au;

    public ChallengesService() throws IOException {
        au = new ApiUtils();
    }

    public Challenge getCurrentChallenge() throws Exception {
        SessionUtils cache = SessionUtils.getInstance();
        if (cache.getChallenge() == null) {

            Object object = au.getObject("/challenge/current");
            if (!(object instanceof Challenge)){
                return null;
            } else {
                return (Challenge) object;
            }
        } else {
            return cache.getChallenge();
        }
    }
}
