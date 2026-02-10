package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.utils.ApiUtils;

import java.io.IOException;

public class ChallengesService {
    ApiUtils au;

    public ChallengesService() throws IOException {
        au = new ApiUtils();
    }

    public Challenge getCurrentChallenge() throws Exception {
        Object object = au.getObject("/challenge/current");
         if (!(object instanceof Challenge)){
            return null;
         } else {
             return (Challenge) object;
         }
    }
}
