package com.frameLab.frameSprite.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PreviewController {

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView editedImageView;


    public void initData(Image original, Image edited) {
        originalImageView.setImage(original);
        editedImageView.setImage(edited);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) originalImageView.getScene().getWindow();
        stage.close();
    }
}