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

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class LoginController {
    public Button demoButton;
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
    public void handleLogin(ActionEvent actionEvent) throws LoginException {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
               return ApiUtils.login(emailField.getText(),passwordField.getText());
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
            errorLabel.setText("An Error happened");
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
}
