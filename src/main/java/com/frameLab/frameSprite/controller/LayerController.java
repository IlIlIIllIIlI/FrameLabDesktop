package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class LayerController {

    public ImageView layerPreview;
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
        layerName.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2){
                TextInputDialog dialog = new TextInputDialog(currentLayer.getName());
                dialog.setTitle("Rename Layer");
                dialog.setHeaderText("Enter a new name for this layer");
                dialog.setContentText("Name :");

                dialog.showAndWait().ifPresent( newName ->{
                    if (!Objects.equals(currentLayer.getName(), newName) &&!newName.isBlank()) {
                            layerName.setText(newName);
                            mainController.renameCanvasLayer(currentLayer.getName(),newName);
                            currentLayer.setName(newName);
                    }
                });


            }
        });

        visibleToggle.setSelected(layer.isVisible);
        opacitySlider.setValue(layer.opacity);
        try {
            Image image = currentLayer.getImage();
            layerPreview.setImage(image);
        } catch (Exception e) {
            System.out.println("aaa : "+e);
        }

    }
}
