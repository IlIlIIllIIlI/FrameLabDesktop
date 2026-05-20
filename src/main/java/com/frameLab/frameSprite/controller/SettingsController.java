package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.service.SettingsService;
import com.frameLab.frameSprite.utils.Actions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SettingsController {
    @FXML
    private Button themeToggle;
    @FXML
    private VBox keybindsContainer;

    public static Scene previousScene;

    public static Runnable callback;

    @FXML
    public void initialize() {
        loadKeybindItems();
        if (Objects.equals(Main.getTheme(), "/atlantafx/base/theme/nord-light.css")) {
            themeToggle.setText("Dark Mode");
        } else {
            themeToggle.setText("Light Mode");
        }
    }


    private void loadKeybindItems() {
        keybindsContainer.getChildren().clear();

        for (Actions action : Actions.values()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/keybind-item.fxml"));
                Node row = loader.load();

                KeybindItemController itemController = loader.getController();
                itemController.initData(action);

                keybindsContainer.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleResetAll(ActionEvent actionEvent) {
        SettingsService.getInstance().resetAllToDefaults();
        loadKeybindItems();
    }

    @FXML
    private void handleGoBack(ActionEvent actionEvent) {
        try {


            if (previousScene != null) {
                Stage stage = (Stage) keybindsContainer.getScene().getWindow();
                stage.setScene(previousScene);
            }

            if (callback != null) {
                callback.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaving(ActionEvent actionEvent) {
        SettingsService.getInstance().save();
    }

    @FXML
    private void handleTheme(ActionEvent actionEvent) {
        themeToggle.setText(Main.changeTheme());
    }
}
