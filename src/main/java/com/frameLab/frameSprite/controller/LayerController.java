package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class LayerController {

    @FXML
    private CheckBox visibleToggle;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Label layerName;

    private SpriteLayer currentLayer;
    private EditorController mainController;


    @FXML
    public void initialize(){
        opacitySlider.valueProperty().addListener((obs, old, next) -> {
            if (currentLayer != null && mainController != null) {
                currentLayer.setOpacity(next.doubleValue()) ;
                mainController.updateLayerOpacity(currentLayer.name, currentLayer.opacity);
            }
        });
    }

    @FXML
    private void handleVisibility(ActionEvent actionEvent) {
        if (currentLayer != null && mainController != null) {
            currentLayer.setVisible(visibleToggle.isSelected());
            mainController.updateLayerVisibility(currentLayer.name, currentLayer.isVisible);
        }
    }

    public void setMainController(EditorController mainController) {
        this.mainController = mainController;
    }

    public void setLayer(SpriteLayer layer) {
        this.currentLayer = layer;

        layerName.setText(layer.name);
        visibleToggle.setSelected(layer.isVisible);
        opacitySlider.setValue(layer.opacity);
    }
}
