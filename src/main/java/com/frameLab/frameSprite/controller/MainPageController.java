package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.service.ChallengesService;
import com.frameLab.frameSprite.utils.ApiUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Objects;

public class MainPageController {
    @FXML
    private Button projectsButton;
    @FXML
    private Label currentChallengeLabel;
    @FXML
    private ImageView currentChallengeImage;
    @FXML
    private Button logOutButton;
    ApiUtils au;
    @FXML
    private Label helloLabel;
    ChallengesService challengesService;


    public void initialize() throws Exception {
        this.au = new ApiUtils();
        this.challengesService = new ChallengesService();
        setLabel();
        setCurrentChallenge();
    }

    private void setCurrentChallenge() throws Exception {
        Challenge currentChallenge = challengesService.getCurrentChallenge();
        if (currentChallenge == null) {
            currentChallengeLabel.setText("There is no challenge for now, come back later");
            Image image = new Image("data:image/jpeg;base64,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");
            currentChallengeImage.setImage(image);
            projectsButton.setVisible(false);
        } else {
            currentChallengeLabel.setText("Current Challenge : "+currentChallenge.getTitle());
            Image image = new Image("http://localhost:8000/public/" + currentChallenge.getImageUrl());
            currentChallengeImage.setImage(image);
        }

    }


    public void setLabel() throws LoginException {
        helloLabel.setText("Hello "+au.getEmail());

    }


    @FXML
    public void handleLogOut(ActionEvent actionEvent) throws Exception {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
               au.logOut();
               return null;
            }

        };

        task.setOnSucceeded(event -> {
            try {
                Main.changeScene("/view/login-view.fxml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread = new Thread(task);
        thread.start();
    }


    public void handleProjects() {
        try {
            Main.changeScene("/view/projects-view.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void test(MouseEvent mouseEvent) {
        System.out.println("yey");
    }
}
