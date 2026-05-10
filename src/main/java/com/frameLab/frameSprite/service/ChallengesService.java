package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;

import java.io.IOException;

public class ChallengesService {
    ApiUtils au;

    public ChallengesService() throws IOException {
    }

    public Challenge getCurrentChallenge() throws Exception {
        SessionUtils cache = SessionUtils.getInstance();
        if (cache.getChallenge() == null) {
            Challenge apiChallenge = ApiUtils.getCurrentChallenge();
            cache.setChallenge(apiChallenge);
            return apiChallenge;
        } else {
            return cache.getChallenge();
        }
    }
}
