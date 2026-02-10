package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.utils.ApiUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class LoginController {
    @FXML
    private Label errorLabel;
    @FXML
    private Button submitButton;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField emailField;
    private ApiUtils au;

    public void initialize() throws IOException {
        au = new ApiUtils();

    }

    @FXML
    public void handleLogin(ActionEvent actionEvent) throws LoginException {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
               return au.login(emailField.getText(),passwordField.getText());
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
}
