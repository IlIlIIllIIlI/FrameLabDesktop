package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.effect.*;
import com.frameLab.frameSprite.effect.filters.*;
import com.frameLab.frameSprite.effect.filters.kernels.BlurFilter;
import com.frameLab.frameSprite.effect.filters.kernels.EdgeFilter;
import com.frameLab.frameSprite.effect.filters.kernels.GaussianBlurFilter;
import com.frameLab.frameSprite.effect.filters.kernels.SharpFilter;
import com.frameLab.frameSprite.effect.filters.others.ReflectFilter;
import com.frameLab.frameSprite.effect.filters.others.VerticalReflectFilter;
import com.frameLab.frameSprite.effect.filters.pixels.BrightnessFilter;
import com.frameLab.frameSprite.effect.filters.pixels.ContrastFilter;
import com.frameLab.frameSprite.effect.filters.pixels.GrayScaleFilter;
import com.frameLab.frameSprite.effect.filters.pixels.SepiaFilter;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.HistoryService;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.service.StorageService;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditorController {
    private static final Logger log = LoggerFactory.getLogger(EditorController.class);
    @FXML
    private Slider contrastSlider;
    @FXML
    private StackPane workspaceVoid;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane zoomContainer;
    @FXML
    private StackPane uiOverlay;
    @FXML
    private Circle brushCursor;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private Slider rotateSlider;
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
        rotateSlider.valueProperty().addListener((obs, old, next) -> {
            if (currentCanvas != null) {
                this.handleApplyRotate(next.intValue());
            }
        });


    }

    public void initData(Project project){
        this.currentProject = project;

        transparentGrid();

        if (project.getLayers()==null){
            project.setLayers(new ArrayList<>());
        }

        if (project.getLayers().isEmpty()){
            loadChallengeBackground();
        } else {
            // If an old project was saved bottom-up, reverse it so top is foreground.
            SpriteLayer firstLayer = project.getLayers().getFirst();
            if (firstLayer.getName() != null && firstLayer.getName().equals("Challenge_Background") && project.getLayers().size() > 1) {
                Collections.reverse(project.getLayers());
            }
        }

        setupWorkspaceDimensions(currentProject.getWidth(), currentProject.getHeight());

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
                    if (event.isControlDown() && event.getCode() == KeyCode.S) {
                        try {
                            handleSave(new ActionEvent());
                        } catch (IOException e) {
                            new Alert(Alert.AlertType.ERROR, "Failed to save file: " + e.getMessage()).showAndWait();
                        }

                        event.consume();
                    }

                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        try {
                            handleExport(new ActionEvent());
                        } catch (IOException e) {
                            new Alert(Alert.AlertType.ERROR, "Failed to Export file: " + e.getMessage()).showAndWait();
                        }

                        event.consume();
                    }

                    if (event.isControlDown()) {
                        Bounds viewportBounds = scrollPane.getViewportBounds();
                        double centerX = viewportBounds.getWidth() / 2.0;
                        double centerY = viewportBounds.getHeight() / 2.0;

                        if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.ADD) {
                            // Zoom In
                            applyZoom(1.1, centerX, centerY);
                            event.consume();
                        }
                        else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                            // Zoom Out
                            applyZoom(1 / 1.1, centerX, centerY);
                            event.consume();
                        }
                        else if (event.getCode() == KeyCode.DIGIT0 || event.getCode() == KeyCode.NUMPAD0) {
                            zoomContainer.setScaleX(1.0);
                            zoomContainer.setScaleY(1.0);
                            scrollPane.setHvalue(0.5);
                            scrollPane.setVvalue(0.5);
                            scrollPane.layout();
                            event.consume();
                        }
                    }
                });


            }

            });

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown() && event.getDeltaY() != 0) {
                event.consume();

                double mouseX = event.getX();
                double mouseY = event.getY();

                if (event.getDeltaY() > 0) {
                    applyZoom(1.1, mouseX, mouseY); // Zoom In
                } else {
                    applyZoom(1 / 1.1, mouseX, mouseY); // Zoom Out
                }
            } else {
                // No scrolling
                if (scrollPane.getStyleClass().contains("hidden-scrollbars")) {
                    event.consume();
                }
                }
        });

        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            updateScrollbarVisibility();
        });



        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(canvasContainer.widthProperty());
        clipRect.heightProperty().bind(canvasContainer.heightProperty());
        uiOverlay.setClip(clipRect);

        canvasContainer.setCursor(Cursor.NONE);
        uiOverlay.setCursor(Cursor.NONE);

        brushCursor.radiusProperty().bind(widthSlider.valueProperty().divide(2));
        brushCursor.strokeProperty().bind(colorPicker.valueProperty());

        canvasContainer.addEventFilter(MouseEvent.MOUSE_MOVED, this::updateCursorPosition);
        canvasContainer.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateCursorPosition);

        canvasContainer.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> brushCursor.setVisible(true));
        canvasContainer.addEventFilter(MouseEvent.MOUSE_EXITED, e -> brushCursor.setVisible(false));

        Platform.runLater(this::fitImageToScreen);


    }

    private void applyZoom(double multiplier, double mouseX, double mouseY) {
        double oldScale = zoomContainer.getScaleX();
        double newScale = oldScale * multiplier;

        if (newScale < 0.1) newScale = 0.1;
        if (newScale > 10.0) newScale = 10.0;
        if (newScale == oldScale) return;

        Point2D mouseInScene = scrollPane.localToScene(mouseX, mouseY);

        Point2D mouseInCanvas = zoomContainer.sceneToLocal(mouseInScene);

        zoomContainer.setScaleX(newScale);
        zoomContainer.setScaleY(newScale);

        scrollPane.layout();

        Point2D newMouseInScene = zoomContainer.localToScene(mouseInCanvas);

        double driftX = newMouseInScene.getX() - mouseInScene.getX();
        double driftY = newMouseInScene.getY() - mouseInScene.getY();

        Bounds viewport = scrollPane.getViewportBounds();
        Bounds content = scrollPane.getContent().getBoundsInLocal();

        double hRange = content.getWidth() - viewport.getWidth();
        double vRange = content.getHeight() - viewport.getHeight();

        if (hRange > 0) {
            double currentScrollPx = scrollPane.getHvalue() * hRange;
            scrollPane.setHvalue(Math.clamp((currentScrollPx + driftX) / hRange, 0, 1));
        }

        if (vRange > 0) {
            double currentScrollPx = scrollPane.getVvalue() * vRange;
            scrollPane.setVvalue(Math.clamp((currentScrollPx + driftY) / vRange, 0, 1));
        }

        updateScrollbarVisibility();
    }

    private void updateCursorPosition(MouseEvent e) {
        double centerX = e.getX() - (canvasContainer.getWidth() / 2);
        double centerY = e.getY() - (canvasContainer.getHeight() / 2);

        brushCursor.setTranslateX(centerX);
        brushCursor.setTranslateY(centerY);
    }

    private void loadChallengeBackground() {
        try {
            Image image = SessionUtils.getInstance().getChallengeImage();


            int projectWidth = (int) image.getWidth();
            int projectHeight = (int) image.getHeight();

            this.currentProject.setWidth(projectWidth);
            this.currentProject.setHeight(projectHeight);

            Canvas bgCanvas = new Canvas(projectWidth, projectHeight);
            GraphicsContext gc = bgCanvas.getGraphicsContext2D();

            gc.drawImage(image, 0,0);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage scaledWritableImage = bgCanvas.snapshot(params, null);

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
            return;
        }

        handleSave(new ActionEvent());

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);


        Background checkerboard = canvasContainer.getBackground();

        canvasContainer.setBackground(null);

        WritableImage previewImage = canvasContainer.snapshot(params, null);

        canvasContainer.setBackground(checkerboard);
        ImageView previewView = new ImageView(previewImage);
        previewView.setFitWidth(400);
        previewView.setPreserveRatio(true);

        Dialog<ButtonType> export = new Dialog<>();
        export.setTitle("Export Options");
        export.setHeaderText("How would you like to export your project?");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(previewView);
        export.getDialogPane().setContent(content);

        ButtonType webButton = new ButtonType("Export to Website");
        ButtonType zipButton = new ButtonType("Save as ZIP");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        export.getDialogPane().getButtonTypes().setAll(webButton, zipButton, cancelButton);

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
            new Alert(Alert.AlertType.ERROR, "Failed to create ZIP file: " + e.getMessage()).showAndWait();
        }
    }
    private void exportToWebsite(){

        Dialog<Void> loadingDialog = new Dialog<>();
        loadingDialog.setTitle("Exporting");
        loadingDialog.setHeaderText("Uploading to the website... Please wait.");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(300);

        VBox loadingContent = new VBox(10, progressBar);
        loadingContent.setAlignment(Pos.CENTER);
        loadingDialog.getDialogPane().setContent(loadingContent);

        loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        loadingDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        Task<Void> uploadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File previewFile = new File("projects/" + currentProject.getId() + "/preview.png");
                if (!previewFile.exists()){
                    throw new RuntimeException("No Preview image");
                }


               int responseCode = ApiUtils.uploadEntry(
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
            loadingDialog.setResult(null);
            loadingDialog.close();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Export was a success!");
                alert.showAndWait();
            });
        });

        uploadTask.setOnFailed(e -> {
            loadingDialog.setResult(null);
            loadingDialog.close();

            Platform.runLater(() -> {
                Throwable error = uploadTask.getException();
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Export failed");
                alert.setHeaderText("The Export failed to reach the website");
                alert.setContentText(error.getMessage());
                alert.showAndWait();
            });
        });

        loadingDialog.setOnShown(event -> {
            new Thread(uploadTask).start();
        });
        loadingDialog.showAndWait();
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

        // Changed for loop signature in order to reverse the list for a good layer rendering order
        for (int i = layerListModel.size() - 1; i >= 0; i--) {
            SpriteLayer layer = layerListModel.get(i);
            Canvas canvas = new Canvas(currentProject.getWidth(), currentProject.getHeight());
            canvas.setId(layer.getName());
            canvas.setOpacity(layer.getOpacity());
            canvas.setVisible(layer.isVisible());
            if (layer.getImage() != null) {
                canvas.getGraphicsContext2D().drawImage(layer.getImage(), 0, 0);
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
            Point2D p = currentCanvas.sceneToLocal(e.getSceneX(), e.getSceneY());
            lastX = p.getX();
            lastY = p.getY();

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
            Point2D p = currentCanvas.sceneToLocal(e.getSceneX(), e.getSceneY());
            double currentX = p.getX();
            double currentY = p.getY();

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

        layerListModel.addFirst(newLayer);
        canvasContainer.getChildren().add(newCanvas);
        syncCanvasOrder();
        layerListView.getSelectionModel().select(newLayer);
    }

    @FXML
    private  void handleDeleteLayer(ActionEvent actionEvent) {
        SpriteLayer selected = layerListView.getSelectionModel().getSelectedItem();
        int selectedIndex = layerListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == layerListModel.size() - 1) {
            return;
        }
        if (selected != null && layerListModel.size() > 1) {
            layerListModel.remove(selected);

            canvasContainer.getChildren().removeIf(node -> node.getId() != null &&
                    node.getId().equals(selected.name));

            syncCanvasOrder();

            if (!layerListModel.isEmpty()) {
                layerListView.getSelectionModel().selectLast();
            }
        }
    }

    @FXML
    private  void handleLayerUp(ActionEvent actionEvent) {
        int selected = layerListView.getSelectionModel().getSelectedIndex();
        if (selected < 1) {
            return;
        }
        int next = selected -1;

        Collections.swap(layerListModel,selected,next);
        syncCanvasOrder();
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
        syncCanvasOrder();
        layerListView.getSelectionModel().select(next);
    }

    public void updateLayerVisibility(String layerName, boolean isVisible) {
        Canvas canvas = getCanvasForLayer(layerName);
        if (canvas != null) {
            canvas.setVisible(isVisible);
        }
    }

    public void updateLayerOpacity(String layerName, double opacity) {
        Canvas canvas = getCanvasForLayer(layerName);
        if (canvas != null) {
            canvas.setOpacity(opacity);
        }
    }

    public void renameCanvasLayer(String oldName, String newName){
        Canvas canvas = getCanvasForLayer(oldName);
        if (canvas != null) {
            canvas.setId(newName);
        }
    }

    @FXML
    private void handleMerge(ActionEvent actionEvent) {
        int selected = layerListView.getSelectionModel().getSelectedIndex();

        int bottom = selected + 1;

        if (bottom >= layerListModel.size()) return;

        SpriteLayer topLayer = layerListModel.get(selected);
        SpriteLayer bottomLayer = layerListModel.get(bottom);

        Canvas topCanvas = getCanvasForLayer(topLayer.getName());
        Canvas bottomCanvas = getCanvasForLayer(bottomLayer.getName());
        if (topCanvas == null || bottomCanvas == null) return;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        Canvas tempCanvas = new Canvas(topCanvas.getWidth(), topCanvas.getHeight());
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();

        if (bottomLayer.isVisible() && bottomLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(bottomLayer.getImage(), 0, 0);
        }

        if (topLayer.isVisible() && topLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(topLayer.getImage(), 0, 0);
        }

        WritableImage mergedImage = tempCanvas.snapshot(parameters, null);

        bottomLayer.setImage(mergedImage);
        bottomLayer.setOpacity(1.0);
        bottomLayer.setVisible(true);

        bottomCanvas.setOpacity(1.0);
        bottomCanvas.setVisible(true);

        GraphicsContext bottomgc = bottomCanvas.getGraphicsContext2D();
        bottomgc.clearRect(0, 0, bottomCanvas.getWidth(), bottomCanvas.getHeight());
        bottomgc.drawImage(mergedImage, 0, 0);

        layerListModel.remove(selected);

        syncCanvasOrder();

        layerListView.getSelectionModel().select(bottomLayer);
    }

    @FXML
    private void handleApplyGrayscale(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new GrayScaleFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplySepia(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new SepiaFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplySharpen(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new SharpFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplyBlur(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new BlurFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplyGaussianBlur(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new GaussianBlurFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplyEdge(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new EdgeFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplyReflect(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new ReflectFilter());

        historyService.addCommand(command);
    }

    @FXML
    private void handleApplyVerticalReflect(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new VerticalReflectFilter());

        historyService.addCommand(command);
    }


    @FXML
    private void handleBrightness(ActionEvent actionEvent) {
        if (currentCanvas == null) return;
        Dialog<Integer> brightness = new Dialog<>();
        brightness.setTitle("Brightness Adjustment");
        brightness.setHeaderText("How would you like to export your project?");

        ButtonType applyButton = new ButtonType("Apply");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    }

    @FXML
    private void handleApplyContrast(ActionEvent actionEvent) {

        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new ContrastFilter((int) contrastSlider.getValue()));

        historyService.addCommand(command);

    }

    @FXML
    private void handleApplyBrightness(ActionEvent actionEvent) {
        if (currentCanvas == null) return;

        FilterCommand command = new FilterCommand(currentCanvas, new BrightnessFilter((int) brightnessSlider.getValue()));

        historyService.addCommand(command);
    }

    private void handleApplyRotate(int rotation) {
        if (currentCanvas == null) return;

        currentCanvas.setRotate(rotation);
    }

    @FXML
    private void handleGoBack(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) widthSlider.getScene().getWindow();
            stage.setTitle("Projects");
            Main.changeScene("/view/projects-view.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Canvas getCanvasForLayer(String layerName) {
        for (Node node : canvasContainer.getChildren()) {
            if (node instanceof Canvas && layerName.equals(node.getId())) {
                return (Canvas) node;
            }
        }
        return null;
    }

    private void syncCanvasOrder() {
        ObservableList<Node> newOrder = FXCollections.observableArrayList();
        // Changed for loop signature in order to reverse the list
        for (int i = layerListModel.size() - 1; i >= 0; i--) {
            SpriteLayer layer = layerListModel.get(i);
            Canvas canvas = getCanvasForLayer(layer.getName());
            if (canvas != null) {
                newOrder.add(canvas);
            }
        }
        canvasContainer.getChildren().setAll(newOrder);
    }

    private void transparentGrid(){
        int squareSize = 30;

        WritableImage patternImage = new WritableImage(squareSize * 2, squareSize * 2);
        PixelWriter writer = patternImage.getPixelWriter();

        Color color1 = Color.TRANSPARENT;
        Color color2 = Color.color(0, 0, 0, 0.15);

        for (int x = 0; x < squareSize * 2; x++) {
            for (int y = 0; y < squareSize * 2; y++) {

                boolean isLight = ((x / squareSize) + (y / squareSize)) % 2 == 0;
                writer.setColor(x, y, isLight ? color1 : color2);
            }
        }

        ImagePattern checkerboard = new ImagePattern(
                patternImage, 0, 0, squareSize * 2, squareSize * 2, false
        );

        canvasContainer.setBackground(new Background(new BackgroundFill(checkerboard, null, null)));


    }

    private long getFolderSize(File folder) {
        long length = 0;
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        length += file.length();
                    } else {
                        length += getFolderSize(file);
                    }
                }
            }
        }
        return length;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format("%.1f %sB", (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }


    @FXML
    private void handleShowInfo(ActionEvent actionEvent) throws IOException {
        if (currentProject == null) return;

        try {
            handleSave(new ActionEvent());
        } catch (IOException e) {
            System.out.printf(String.valueOf(e));
        }

        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Project Details");
        infoAlert.setHeaderText("Information for: " + (currentProject.getTitle() != null ? currentProject.getTitle() : "Untitled"));

        int width = currentProject.getWidth();
        int height = currentProject.getHeight();
        int layerCount = layerListModel.size();

        int gcd = getGCD(width, height);
        String aspectRatio = (width / gcd) + ":" + (height / gcd);

        File projectDir = new File("projects/" + currentProject.getId());
        long sizeInBytes = getFolderSize(projectDir);
        String readableSize = formatFileSize(sizeInBytes);

        String lastModified = "Unknown";
        if (projectDir.exists()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm");
            lastModified = sdf.format(projectDir.lastModified());
        }

        String author = "Local Guest";
        if (SessionUtils.getInstance().getUser() != null && SessionUtils.getInstance().getUser().getId() != -1) {
            author = SessionUtils.getInstance().getUser().getFirstName();
        }

        String details = String.format(
                        "Author:\t\t %s\n" +
                        "Dimensions:\t %d x %d px  (%s)\n" +
                        "Total Layers:\t %d\n\n" +
                        "Disk Size:\t\t %s\n" +
                        "Last Modified:\t %s\n",
                author, width, height, aspectRatio, layerCount, readableSize, lastModified
        );

        infoAlert.setContentText(details);
        infoAlert.showAndWait();
    }

    private int getGCD(int p, int q) {
        if (q == 0) return p;
        return getGCD(q, p % q);
    }

    @FXML
    private void handleShowPreview(ActionEvent actionEvent) {
        Image originalImage = null;
        try {
            originalImage = SessionUtils.getInstance().getChallengeImage();
        } catch (Exception e) {
            System.out.printf(String.valueOf(e));

        }

        if (originalImage == null) {
            new Alert(Alert.AlertType.WARNING, "No original challenge image found to compare against!").showAndWait();
            return;
        }

        Background checkerboard = canvasContainer.getBackground();
        canvasContainer.setBackground(null);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage editedImage = canvasContainer.snapshot(params, null);

        canvasContainer.setBackground(checkerboard);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/preview-item.fxml"));
            javafx.scene.Parent root = loader.load();

            PreviewController controller = loader.getController();
            controller.initData(originalImage, editedImage);

            Stage previewStage = new Stage();
            previewStage.setTitle("Before & After");
            previewStage.setScene(new javafx.scene.Scene(root));

            previewStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            previewStage.show();

        } catch (IOException e) {
            System.out.printf(String.valueOf(e));
            new Alert(Alert.AlertType.ERROR, "Failed to load the Preview Window.").showAndWait();
        }
    }

    private void updateScrollbarVisibility() {
        Bounds viewport = scrollPane.getViewportBounds();
        if (viewport == null) return;

        double currentScale = zoomContainer.getScaleX();

        double physicalCanvasWidth = currentProject.getWidth() * currentScale;
        double physicalCanvasHeight = currentProject.getHeight() * currentScale;

        boolean isSmallerThanScreen = (physicalCanvasWidth <= viewport.getWidth()) &&
                (physicalCanvasHeight <= viewport.getHeight());

        if (isSmallerThanScreen) {
            if (!scrollPane.getStyleClass().contains("hidden-scrollbars")) {
                scrollPane.getStyleClass().add("hidden-scrollbars");
                scrollPane.setHvalue(0.5);
                scrollPane.setVvalue(0.5);
                scrollPane.setHvalue(0.5);
                scrollPane.setVvalue(0.5);

            }
        } else {
            scrollPane.getStyleClass().remove("hidden-scrollbars");
        }
    }

private void setupWorkspaceDimensions(double width, double height) {
    zoomContainer.setMaxSize(width, height);
    zoomContainer.setPrefSize(width, height);

    canvasContainer.setMaxSize(width, height);
    canvasContainer.setPrefSize(width, height);

    uiOverlay.setMaxSize(width, height);
    uiOverlay.setPrefSize(width, height);
    double maxDimension = Math.max(width, height);

    double dynamicMaxBrush = Math.max(50.0, maxDimension * 0.05);

    widthSlider.setMax(dynamicMaxBrush);

    widthSlider.setValue(dynamicMaxBrush * 0.1);

    double dynamicStrokeWidth = Math.max(1.5, maxDimension * 0.001);
    brushCursor.setStrokeWidth(dynamicStrokeWidth);


}

    private void fitImageToScreen() {
        Bounds viewport = scrollPane.getViewportBounds();

        if (viewport == null || viewport.getWidth() == 0) {
            Platform.runLater(this::fitImageToScreen);
            return;
        }

        double padding = 0.90;

        double scaleX = (viewport.getWidth() * padding) / currentProject.getWidth();
        double scaleY = (viewport.getHeight() * padding) / currentProject.getHeight();

        double fitScale = Math.min(scaleX, scaleY);

        if (fitScale > 1.0) {
            fitScale = 1.0;
        }

        zoomContainer.setScaleX(fitScale);
        zoomContainer.setScaleY(fitScale);

        scrollPane.layout();
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);

        updateScrollbarVisibility();
    }

    @FXML
    private void handleNuke(ActionEvent actionEvent) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Nuke Project");
        confirm.setHeaderText("⚠ RESET EVERYTHING ⚠");
        confirm.setContentText("Are you sure you want to nuke this project? All custom layers will be destroyed and this CANNOT be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            this.historyService = new HistoryService();

            layerListModel.clear();
            canvasContainer.getChildren().clear();

            loadChallengeBackground();

            List<SpriteLayer> safeCopy = new ArrayList<>(currentProject.getLayers());

            layerListModel.setAll(safeCopy);

            loadImage();

            layerListView.getSelectionModel().selectFirst();

            Platform.runLater(this::fitImageToScreen);

            Platform.runLater(() -> {
                try {
                    handleSave(new ActionEvent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }
}



// @TODO TDD
