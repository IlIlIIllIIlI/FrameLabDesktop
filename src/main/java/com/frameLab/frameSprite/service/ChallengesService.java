package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
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

    public SpriteLayer generateChallengeLayer(int targetWidth, int targetHeight, String layerName) throws Exception {
        Image image = SessionUtils.getInstance().getChallengeImage();
        if (image == null) {
            throw new IllegalStateException("No challenge image loaded in session.");
        }

        double scaleX = (double) targetWidth / image.getWidth();
        double scaleY = (double) targetHeight / image.getHeight();
        double scale = Math.min(scaleX, scaleY);

        double finalWidth = image.getWidth() * scale;
        double finalHeight = image.getHeight() * scale;
        double x = (targetWidth - finalWidth) / 2;
        double y = (targetHeight - finalHeight) / 2;

        Canvas resizeCanvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = resizeCanvas.getGraphicsContext2D();
        gc.drawImage(image, x, y, finalWidth, finalHeight);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage scaledWritableImage = resizeCanvas.snapshot(params, null);

        SpriteLayer challengeLayer = new SpriteLayer(layerName, targetWidth, targetHeight);
        challengeLayer.setImage(scaledWritableImage);

        return challengeLayer;
    }



    public void uploadChallengeEntry(Project project, File previewFile) throws Exception {
        if (!previewFile.exists()) {
            throw new RuntimeException("No Preview image found to upload.");
        }

        int responseCode = ApiUtils.uploadEntry(
                SessionUtils.getInstance().getUser().getId(),
                project.getChallengeId(),
                previewFile
        );

        if (responseCode == 404) {
            throw new IllegalStateException("You already have an Entry for this challenge");
        } else if (responseCode == 401) {
            throw new IllegalStateException("Session Expired, please reconnect");
        } else if (responseCode != 200) {
            throw new RuntimeException("Something happened, please try later.");
        }
    }


}
