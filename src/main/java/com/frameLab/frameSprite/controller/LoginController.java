package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Application;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Properties;

public class LoginController {
    @FXML
    private Label errorLabel;
    @FXML
    private Button submitButton;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField emailField;

    public void initialize() throws IOException {
    }

    @FXML
    private void handleLogin(ActionEvent actionEvent) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {

                ApiUtils.login(emailField.getText(),passwordField.getText());

                User loggedInUser = ApiUtils.getMe();

                SessionUtils.getInstance().setUser(loggedInUser);
                return null;
            }

        };

        task.setOnSucceeded(event -> {
            try {
                Main.changeScene("/view/main-page-view.fxml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        task.setOnFailed( event -> {
            Throwable error = task.getException();
            errorLabel.setText(error.getMessage());
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    public void handleDemo(ActionEvent actionEvent) throws IOException {
        User demoUser = new User();
        demoUser.setId(-1);
        demoUser.setFirst_name("Guest");

        Challenge demoChallenge = new Challenge();
        demoChallenge.setId(-1);

        SessionUtils.getInstance().setUser(demoUser);
        SessionUtils.getInstance().setChallenge(demoChallenge);

        try {
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setTitle("Projects");
            Main.changeScene("/view/projects-view.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void handleCreate(ActionEvent actionEvent) throws IOException {
        Properties config = new Properties();
        config.load(getClass().getResourceAsStream("/config.properties"));

        Main.openWebsite(config.getProperty("website")+"/register");
    }
}
