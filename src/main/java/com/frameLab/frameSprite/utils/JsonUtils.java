package com.frameLab.frameSprite.utils;

import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.User;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {
    static class Parsing {
        User user;
        String message;
        boolean success;
        Challenge challenge;
        static SessionUtils cache;

        static {
            try {
                cache = SessionUtils.getInstance();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void setMessage(String message) {
            this.message = message;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public User getUser() {
            cache.setUser(user);
            return user;
        }

        public Object getMainClass(){
            if (user != null) {
                return getUser();
            }

            if (challenge != null) {
                return getChallenge();
            }

            return message;
        }

        public Challenge getChallenge() {
            cache.setChallenge(challenge);
            return challenge;
        }
    }
    public static String mapToJson(Map<String, String> data) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!first) jsonBuilder.append(",");
            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}
