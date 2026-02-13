package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.effect.Paint;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.HistoryService;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;

public class EditorController {
    @FXML
    private ListView<SpriteLayer> layerListView;
    @FXML
    private StackPane canvasContainer;

    private HistoryService historyService;
    private Project currentProject;
    private Canvas currentCanvas;
    private Paint currentPaintCommand;

    private ObservableList<SpriteLayer> layerListModel;

    private ProjectsService projectsService;
    public void initialize(){
        this.historyService = new HistoryService();
        this.projectsService = new ProjectsService();
    }

    public void initData(Project project){
        this.currentProject = project;

        if (project.getLayers()==null){
            project.setLayers(new ArrayList<>());
        }

        if (project.getLayers().isEmpty()){
            loadChallengeBackground();
        }

        this.layerListModel = FXCollections.observableList(project.getLayers());

        layerListView.setItems(layerListModel);

        layerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setActiveLayer(newVal);
            }
        });

        loadImage();

        layerListView.getSelectionModel().selectFirst();

        drawing();
    }

    private void loadChallengeBackground() {
        try {
            Challenge challenge = SessionUtils.getInstance().getChallenge();

            String imageUrl = "http://localhost:8000/public/" + challenge.getImageUrl();

            Image image = new Image(imageUrl);

            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            this.currentProject.setWidth(width);
            this.currentProject.setHeight(height);

            SpriteLayer bgLayer = new SpriteLayer("Challenge_Background", width, height);

            bgLayer.setImage(new WritableImage(image.getPixelReader(),width,height));

            this.currentProject.getLayers().add(bgLayer);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSave(ActionEvent actionEvent) throws IOException {
        projectsService.saveProject(currentProject);
    }

    @FXML
    private void handleExport(ActionEvent actionEvent) {

    }


    private void setActiveLayer(SpriteLayer layer){
        for(Node node : canvasContainer.getChildren()){
            if (node instanceof Canvas && node.getId().equals(layer.name)) {
                this.currentCanvas = (Canvas) node;
                drawing();
            }
        }
    }


    private void loadImage(){
        canvasContainer.getChildren().clear();

        for (SpriteLayer layer : currentProject.getLayers()){
            Canvas canvas = new Canvas(800,600);
            canvas.setId(layer.name);

            if (layer.image != null) {
                canvas.getGraphicsContext2D().drawImage(layer.image, 0, 0);
            }

            canvasContainer.getChildren().add(canvas);

        }
    }

    private void drawing() {
        GraphicsContext gc = currentCanvas.getGraphicsContext2D();
        gc.setLineWidth(5.0);
        gc.setStroke(Color.BLACK);

        canvasContainer.setOnMousePressed(e -> {
            currentPaintCommand = new Paint(currentCanvas, 0,0,currentCanvas.getWidth(),currentCanvas.getHeight());
            gc.beginPath();
            gc.moveTo(e.getX(),e.getY());
            gc.stroke();
        });

        canvasContainer.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        canvasContainer.setOnMouseReleased(e -> {
            if (currentPaintCommand != null) {
                currentPaintCommand.savePresent();
                historyService.addCommand(currentPaintCommand);
                currentPaintCommand = null;
            }
        });
    }


    @FXML
    private void handleUndo(ActionEvent actionEvent) {
        historyService.undo();
    }

    @FXML
    private void handleRedo(ActionEvent actionEvent) {
        historyService.redo();
    }

    @FXML
    private void handleAddLayer(ActionEvent actionEvent) {
        String newName = "Layer " + (layerListModel.size() + 1);
        SpriteLayer newLayer = new SpriteLayer(newName, 800, 600);

        layerListModel.add(newLayer);

        Canvas newCanvas = new Canvas(800, 600);
        newCanvas.setId(newName);
        canvasContainer.getChildren().add(newCanvas);

        layerListView.getSelectionModel().select(newLayer);
    }

    @FXML
    private  void handleDeleteLayer(ActionEvent actionEvent) {
        SpriteLayer selected = layerListView.getSelectionModel().getSelectedItem();
        int selectedIndex = layerListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0) {
            return;
        }
        if (selected != null && layerListModel.size() > 1) {
            layerListModel.remove(selected);

            canvasContainer.getChildren().removeIf(node -> node.getId() != null &&
                    node.getId().equals(selected.name));

            if (!layerListModel.isEmpty()) {
                layerListView.getSelectionModel().selectLast();
            }
        }
    }

    @FXML
    private  void handleLayerUp(ActionEvent actionEvent) {
    }

    @FXML
    private  void handleLayerDown(ActionEvent actionEvent) {
    }
}
