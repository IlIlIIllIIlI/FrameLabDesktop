package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.effect.FilterCommand;
import com.frameLab.frameSprite.effect.GrayScaleFilter;
import com.frameLab.frameSprite.effect.Paint;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.HistoryService;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.service.StorageService;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class EditorController {
    private static final Logger log = LoggerFactory.getLogger(EditorController.class);
    @FXML
    private ToggleButton eraserToggle;
    @FXML
    private Slider widthSlider;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ListView<SpriteLayer> layerListView;
    @FXML
    private StackPane canvasContainer;

    private HistoryService historyService;
    private Project currentProject;
    private Canvas currentCanvas;
    private Paint currentPaintCommand;
    private double lastX;
    private double lastY;

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
        layerListView.setCellFactory(c ->new ListCell<SpriteLayer>() {

            private final HBox root;
            private final LayerController layerController;

            {
                try {
                    FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/layer-item.fxml"));
                    root = loader.load();
                    layerController = loader.getController();
                    layerController.setMainController(EditorController.this);
                } catch (java.io.IOException e) {
                    throw new RuntimeException();
                }

            }

            @Override
            protected void updateItem(SpriteLayer spriteLayer, boolean empty) {
                super.updateItem(spriteLayer,empty);


                if (empty || spriteLayer == null) {
                    setGraphic(null);
                } else {
                    layerController.setLayer(spriteLayer);
                    setGraphic(root);
                }
            }
        });
        layerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setActiveLayer(newVal);
            }
        });


        loadImage();

        colorPicker.setValue(Color.BLACK);
        widthSlider.setValue(5.0);
        layerListView.getSelectionModel().selectFirst();

        drawing();

        canvasContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                        if (event.isShiftDown()) {
                            handleRedo(new ActionEvent());
                        } else {
                            handleUndo(new ActionEvent());
                        }

                        event.consume();
                    }
                });
            }

            });

    }

    private void loadChallengeBackground() {
        try {
            Image image = SessionUtils.getInstance().getChallengeImage();


            int projectWidth = 800;
            int projectHeight = 600;
            this.currentProject.setWidth(projectWidth);
            this.currentProject.setHeight(projectHeight);

            double scaleX = (double) projectWidth / image.getWidth();
            double scaleY = (double) projectHeight / image.getHeight();
            double scale = Math.min(scaleX, scaleY);

            double finalWidth = image.getWidth() * scale;
            double finalHeight = image.getHeight() * scale;

            double x = (projectWidth - finalWidth) / 2;
            double y = (projectHeight - finalHeight) / 2;

            Canvas resizeCanvas = new Canvas(projectWidth, projectHeight);
            GraphicsContext gc = resizeCanvas.getGraphicsContext2D();

            gc.drawImage(image, x, y, finalWidth, finalHeight);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage scaledWritableImage = resizeCanvas.snapshot(params, null);

            SpriteLayer bgLayer = new SpriteLayer("Challenge_Background", projectWidth, projectHeight);
            bgLayer.setImage(scaledWritableImage);

            this.currentProject.getLayers().add(bgLayer);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSave(ActionEvent actionEvent) throws IOException {
        for (Node node: canvasContainer.getChildren()){
            if (node instanceof Canvas canvas){

                for (SpriteLayer layer : layerListModel){
                    if (layer.getName().equals(canvas.getId())){

                        boolean ogVisible = layer.isVisible();
                        double ogOpacity = layer.getOpacity();

                        canvas.setVisible(true);
                        canvas.setOpacity(1.0);

                        SnapshotParameters params = new SnapshotParameters();
                        params.setFill(Color.TRANSPARENT);
                        WritableImage newImage = canvas.snapshot(params, null);

                        layer.setImage(newImage);

                        canvas.setOpacity(ogOpacity);
                        canvas.setVisible(ogVisible);
                    }
                }
            }


        }
        projectsService.saveProject(currentProject);
    }

    @FXML
    private void handleExport(ActionEvent actionEvent) throws IOException {
        if (SessionUtils.getInstance().getUser().getId() == -1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Demo");
            alert.setHeaderText("No Export");
            alert.setContentText("You can't export while in Demo Mode");
            alert.showAndWait();
        }

        handleSave(new ActionEvent());

        Alert export = new Alert(Alert.AlertType.CONFIRMATION);
        export.setTitle("Export Options");
        export.setHeaderText("How would you like to export your project?");

        ButtonType webButton = new ButtonType("Export to Website");
        ButtonType zipButton = new ButtonType("Save as ZIP");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        export.getButtonTypes().setAll(webButton, zipButton, cancelButton);

        Optional<ButtonType> result = export.showAndWait();

        if (result.isPresent()) {
            if (result.get() == webButton) {
                exportToWebsite();
            } else if (result.get() == zipButton) {
                exportAsZip();
            }
        }

    }

    private void exportAsZip() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project as ZIP");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));

        String defaultName = "Project_" + currentProject.getId() + ".zip";
        if (currentProject.getTitle() != null && !currentProject.getTitle().isEmpty()) {
            defaultName = currentProject.getTitle().replaceAll("\\s+", "_") + "_Project.zip";
        }
        fileChooser.setInitialFileName(defaultName);

        File zipFile = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());
        if (zipFile == null) return;

        try {
            StorageService storageService = new StorageService();
            storageService.exportProjectAsZip(currentProject, zipFile);

            new Alert(Alert.AlertType.INFORMATION, "Project successfully exported as ZIP!").showAndWait();

        } catch (Exception e) {
            log.error("AAA", e);
            new Alert(Alert.AlertType.ERROR, "Failed to create ZIP file: " + e.getMessage()).showAndWait();
        }
    }
    private void exportToWebsite(){
        Task<Void> uploadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File previewFile = new File("projects/" + currentProject.getId() + "/preview.png");
                if (!previewFile.exists()){
                    throw new RuntimeException("No Preview image");
                }

                ApiUtils apiUtils = new ApiUtils();

                int responseCode = apiUtils.uploadEntry(
                        SessionUtils.getInstance().getUser().getId(),
                        currentProject.getChallengeId(),
                        previewFile
                );

                if (responseCode == 404) {
                    throw new IllegalStateException("You already have an Entry for this challenge");
                } else if (responseCode == 401) {
                    throw new IllegalStateException("Session Expired, please reconnect");
                } else if (responseCode != 200) {
                    throw new RuntimeException("Something happened please try later ");
                }

                return null;

            }
        };

        uploadTask.setOnSucceeded(e -> {
            new Alert(Alert.AlertType.INFORMATION, "Export was a success !").showAndWait();
        });

        uploadTask.setOnFailed(e -> {
            Throwable error = uploadTask.getException();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Export failed");
            alert.setHeaderText("The Export failed to reach the website");
            alert.setContentText(error.getMessage());
            alert.showAndWait();
        });

        new Thread(uploadTask).start();
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
            canvas.setOpacity(layer.getOpacity());
            canvas.setVisible(layer.isVisible());
            if (layer.image != null) {
                canvas.getGraphicsContext2D().drawImage(layer.image, 0, 0);
            }

            canvasContainer.getChildren().add(canvas);

        }
    }

    private void drawing() {
        GraphicsContext gc = currentCanvas.getGraphicsContext2D();

        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        canvasContainer.setOnMousePressed(e -> {
            currentPaintCommand = new Paint(currentCanvas, 0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

            double size = widthSlider.getValue();
            lastX = e.getX();
            lastY = e.getY();

            if (eraserToggle != null && eraserToggle.isSelected()) {
                gc.clearRect(lastX - size / 2, lastY - size / 2, size, size);
            } else {
                gc.setStroke(colorPicker.getValue());
                gc.setLineWidth(size);
                gc.beginPath();
                gc.moveTo(lastX, lastY);
                gc.stroke();
            }
        });

        canvasContainer.setOnMouseDragged(e -> {
            double size = widthSlider.getValue();
            double currentX = e.getX();
            double currentY = e.getY();

            if (eraserToggle != null && eraserToggle.isSelected()) {

                double distance = Math.hypot(currentX - lastX, currentY - lastY);

                int steps = (int) (distance / (size / 4)) + 1;
                double dx = (currentX - lastX) / steps;
                double dy = (currentY - lastY) / steps;

                for (int i = 0; i < steps; i++) {
                    gc.clearRect(lastX + (dx * i) - size / 2, lastY + (dy * i) - size / 2, size, size);
                }
            } else {
                gc.setStroke(colorPicker.getValue());
                gc.setLineWidth(size);
                gc.lineTo(currentX, currentY);
                gc.stroke();
            }

            lastX = currentX;
            lastY = currentY;
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Layer");
        alert.setHeaderText("What kind of layer do you need ?");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType challengeButton = new ButtonType("Challenge Layer");
        ButtonType empty = new ButtonType("Empty Layer");

        alert.getButtonTypes().setAll(empty,challengeButton,cancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == cancel) {
                return;
        }

        String newName = "Layer " + (layerListModel.size() + 1);
        SpriteLayer newLayer = new SpriteLayer(newName, 800, 600);

        Canvas newCanvas = new Canvas(currentProject.getWidth(), currentProject.getHeight());
        newCanvas.setId(newName);

        if ( result.get() == challengeButton) {
            try {
                Image image= SessionUtils.getInstance().getChallengeImage();
                int projectWidth = 800;
                int projectHeight = 600;
                this.currentProject.setWidth(projectWidth);
                this.currentProject.setHeight(projectHeight);

                double scaleX = (double) projectWidth / image.getWidth();
                double scaleY = (double) projectHeight / image.getHeight();
                double scale = Math.min(scaleX, scaleY);

                double finalWidth = image.getWidth() * scale;
                double finalHeight = image.getHeight() * scale;

                double x = (projectWidth - finalWidth) / 2;
                double y = (projectHeight - finalHeight) / 2;

                Canvas resizeCanvas = new Canvas(projectWidth, projectHeight);
                GraphicsContext gc = resizeCanvas.getGraphicsContext2D();

                gc.drawImage(image, x, y, finalWidth, finalHeight);

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);

                WritableImage scaledWritableImage = resizeCanvas.snapshot(params, null);


                newLayer.setImage(scaledWritableImage);

                newCanvas.getGraphicsContext2D().drawImage(scaledWritableImage, 0, 0);

            } catch(Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("ERROR");
                error.setHeaderText("Image fail to load");
                error.showAndWait();
            }
        }

        layerListModel.add(newLayer);
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
        int selected = layerListView.getSelectionModel().getSelectedIndex();
        if (selected <1 || selected >= layerListModel.size() -1 ) {
            return;
        }
        int next = selected -1;

        Collections.swap(layerListModel,selected,next);
        Node temp = canvasContainer.getChildren().remove(selected);
        canvasContainer.getChildren().add(next,temp);

        layerListView.getSelectionModel().select(next);
    }

    @FXML
    private  void handleLayerDown(ActionEvent actionEvent) {
        int selected = layerListView.getSelectionModel().getSelectedIndex();
        if (selected <0 || selected >= layerListModel.size() -1 ) {
            return;
        }
        int next = selected +1;

        Collections.swap(layerListModel,selected,next);
        Node temp = canvasContainer.getChildren().remove(selected);
        canvasContainer.getChildren().add(next,temp);
        layerListView.getSelectionModel().select(next);
    }

    public void updateLayerVisibility(String layerName, boolean isVisible) {
        for (Node node : canvasContainer.getChildren()) {
            if (node instanceof Canvas && layerName.equals(node.getId())) {
                node.setVisible(isVisible);
                return;
            }
        }
    }

    public void updateLayerOpacity(String layerName, double opacity) {
        for (Node node : canvasContainer.getChildren()) {
            if (node instanceof Canvas && layerName.equals(node.getId())) {
                node.setOpacity(opacity);
                return;
            }
        }
    }

    public void renameCanvasLayer(String oldName, String newName){
        for (Node node: canvasContainer.getChildren()){
            if (node instanceof Canvas && Objects.equals(oldName, node.getId())){
                node.setId(newName);
                return;
            }
        }
    }

    @FXML
    private void handleMerge(ActionEvent actionEvent) {
        int selected = layerListView.getSelectionModel().getSelectedIndex();

        int bottom = selected -1;

        if (bottom < 0 ) return;


        SpriteLayer topLayer = layerListModel.get(selected);
        SpriteLayer bottomLayer = layerListModel.get(bottom);

        Canvas bottomCanvas = (Canvas) canvasContainer.getChildren().get(bottom);
        Canvas topCanvas = (Canvas) canvasContainer.getChildren().get(selected);

        double topOpacity = topCanvas.getOpacity();
        double botOpacity = bottomCanvas.getOpacity();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        topCanvas.setOpacity(1.0);
        bottomCanvas.setOpacity(1.0);

        topLayer.setImage(topCanvas.snapshot(parameters,null));
        bottomLayer.setImage(bottomCanvas.snapshot(parameters,null));

        topCanvas.setOpacity(topOpacity);
        bottomCanvas.setOpacity(botOpacity);

        Canvas tempCanvas = new Canvas(currentCanvas.getWidth(),currentCanvas.getHeight());
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();
        if (bottomLayer.isVisible() && bottomLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(bottomLayer.getImage(),0,0);
        }

        if (topLayer.isVisible() && topLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(topLayer.getImage(),0,0);
        }

        WritableImage mergedImage = tempCanvas.snapshot(parameters,null);

        bottomLayer.setImage(mergedImage);
        bottomLayer.setOpacity(1.0);
        bottomLayer.setVisible(true);

        bottomCanvas.setOpacity(1.0);
        bottomCanvas.setVisible(true);

        GraphicsContext bottomgc = bottomCanvas.getGraphicsContext2D();
        bottomgc.clearRect(0,0,bottomCanvas.getWidth(),bottomCanvas.getHeight());
        bottomgc.drawImage(mergedImage,0,0);

        layerListModel.remove(selected);
        canvasContainer.getChildren().remove(selected);

        layerListView.getSelectionModel().select(bottom);
    }

    @FXML
    private void handleApplyGrayscale(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new GrayScaleFilter());

        historyService.addCommand(command);
    }
}


// @TODO ajustement globaux, filtres, geo, supprimer projet TDD
