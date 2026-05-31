package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.effect.*;
import com.frameLab.frameSprite.effect.Paint;
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
import com.frameLab.frameSprite.service.*;
import com.frameLab.frameSprite.utils.Actions;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditorController {
    private static final Logger log = LoggerFactory.getLogger(EditorController.class);
    @FXML
    private Text textCursor;
    @FXML
    private BorderPane editorRoot;
    @FXML
    private  ToggleButton panToggle;
    @FXML
    private  ToggleButton brushToggle;
    @FXML
    private ToggleGroup toolGroup;
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
    @FXML
    private ToggleButton shapeToggle;
    @FXML
    private ComboBox<String> shapeTypeComboBox;
    @FXML
    private ToggleButton shapeFillToggle;
    @FXML
    private ColorPicker secondaryColorPicker;
    @FXML
    private ToggleButton gradientToggle;

    @FXML
    private ToggleButton textToggle;
    @FXML
    private TextField textInputField;


    private ChallengesService challengesService;
    private HistoryService historyService;
    private Project currentProject;
    private Canvas currentCanvas;
    private Paint currentPaintCommand;
    private double lastX;
    private double lastY;

    private double panStartX, panStartY;
    private double panStartHval, panStartVval;
    private boolean isPanning = false;
    private double shapeStartX, shapeStartY;
    private Image preShapeSnapshot;


    private ObservableList<SpriteLayer> layerListModel;

    private ProjectsService projectsService;

    private Project originalProject;

    public void initialize() throws IOException {
        this.historyService = new HistoryService();
        this.projectsService = new ProjectsService();
        this.challengesService = new ChallengesService();

        toolGroup = new ToggleGroup();
        brushToggle.setToggleGroup(toolGroup);
        eraserToggle.setToggleGroup(toolGroup);
        panToggle.setToggleGroup(toolGroup);
        shapeToggle.setToggleGroup(toolGroup);
        gradientToggle.setToggleGroup(toolGroup);
        textToggle.setToggleGroup(toolGroup);

        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }

            if (panToggle.isSelected()) {
                brushCursor.setVisible(false);
                canvasContainer.setCursor(Cursor.OPEN_HAND);
            } else {
                canvasContainer.setCursor(Cursor.NONE);
            }
        });

        rotateSlider.valueProperty().addListener((obs, old, next) -> {
            if (currentCanvas != null) {
                this.handleApplyRotate(next.intValue());
            }
        });


        Platform.runLater(() -> setupManualTooltips(editorRoot));

    }

    public void initData(Project project){
        this.originalProject = project;
        this.currentProject = copy(project);
        transparentGrid();

        if (currentProject.getLayers()==null){
            currentProject.setLayers(new ArrayList<>());
        }

        if (currentProject.getLayers().isEmpty()){
            loadChallengeBackground();
        } else {
            // If an old project was saved bottom-up, reverse it so top is foreground.
            SpriteLayer firstLayer = currentProject.getLayers().getFirst();
            if (firstLayer.getName() != null && firstLayer.getName().equals("Challenge_Background") && project.getLayers().size() > 1) {
                Collections.reverse(currentProject.getLayers());
            }
        }

        setupWorkspaceDimensions(currentProject.getWidth(), currentProject.getHeight());

        this.layerListModel = FXCollections.observableList(currentProject.getLayers());

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
                setupGlobalKeybinds();
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
                    // event.consume(); temp removal to test feelings
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

        canvasContainer.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            if (textToggle.isSelected()) {
                textCursor.setVisible(true);
            } else {
                brushCursor.setVisible(true);
            }
        });

        canvasContainer.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            brushCursor.setVisible(false);
            textCursor.setVisible(false);
        });

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
        if (panToggle.isSelected() || isPanning) {
            return;
        }

        double centerX = e.getX() - (canvasContainer.getWidth() / 2);
        double centerY = e.getY() - (canvasContainer.getHeight() / 2);

        if (textToggle != null && textToggle.isSelected()) {
            brushCursor.setVisible(false);
            textCursor.setVisible(true);

            textCursor.setTranslateX(centerX);
            textCursor.setTranslateY(centerY);
            textCursor.setText(textInputField.getText());
            textCursor.setFont(Font.font(widthSlider.getValue() * 3));
            textCursor.setFill(colorPicker.getValue());
        } else {
            textCursor.setVisible(false);
            brushCursor.setVisible(true);

            brushCursor.setTranslateX(centerX);
            brushCursor.setTranslateY(centerY);
        }
    }

    private void loadChallengeBackground() {
        try {
            Image image = SessionUtils.getInstance().getChallengeImage();


            int projectWidth = (int) image.getWidth();
            int projectHeight = (int) image.getHeight();

            this.currentProject.setWidth(projectWidth);
            this.currentProject.setHeight(projectHeight);

            SpriteLayer bgLayer = challengesService.generateChallengeLayer(projectWidth, projectHeight, "Challenge_Background");
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
        originalProject.setWidth(currentProject.getWidth());
        originalProject.setHeight(currentProject.getHeight());
        originalProject.setLayers(new ArrayList<>(currentProject.getLayers()));

        projectsService.saveProject(originalProject);    }

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
        ButtonType pictureButton = new ButtonType("Save as Picture");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        export.getDialogPane().getButtonTypes().setAll(webButton, zipButton,pictureButton, cancelButton);

        Optional<ButtonType> result = export.showAndWait();

        if (result.isPresent()) {
            if (result.get() == webButton) {
                exportToWebsite();
            } else if (result.get() == zipButton) {
                exportAsZip();
            } else if (result.get() == pictureButton) {
                exportAsPicture(previewImage);
            }

        }

    }

    private void exportAsPicture(WritableImage image) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        String defaultName = "Project_" + currentProject.getId() + ".png";
        if (currentProject.getTitle() != null && !currentProject.getTitle().isEmpty()) {
            defaultName = currentProject.getTitle().replaceAll("\\s+", "_") + "_Export.png";
        }
        fileChooser.setInitialFileName(defaultName);

        File imageFile = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());
        if (imageFile == null) return;

        try {
            StorageService storageService = new StorageService();
            storageService.exportProjectAsImage(image, imageFile);

            new Alert(Alert.AlertType.INFORMATION, "Project successfully exported as a Picture!").showAndWait();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save picture: " + e.getMessage()).showAndWait();
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


        canvasContainer.setOnMousePressed(e -> {
            if (currentCanvas == null) return;

            GraphicsContext gc = currentCanvas.getGraphicsContext2D();

            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineJoin(StrokeLineJoin.ROUND);

            if (e.getButton() == MouseButton.MIDDLE || (e.getButton() == MouseButton.PRIMARY && panToggle.isSelected())) {
                isPanning = true;
                panStartX = e.getSceneX();
                panStartY = e.getSceneY();
                panStartHval = scrollPane.getHvalue();
                panStartVval = scrollPane.getVvalue();

                brushCursor.setVisible(false);
                canvasContainer.setCursor(Cursor.CLOSED_HAND);
                return;
            }

            if (e.getButton() == MouseButton.PRIMARY && (brushToggle.isSelected() || eraserToggle.isSelected() || shapeToggle.isSelected() || gradientToggle.isSelected()||textToggle.isSelected())) {
                currentPaintCommand = new Paint(currentCanvas, 0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                double size = widthSlider.getValue();
                Point2D p = currentCanvas.sceneToLocal(e.getSceneX(), e.getSceneY());
                lastX = p.getX();
                lastY = p.getY();

                if (eraserToggle != null && eraserToggle.isSelected()) {
                    gc.clearRect(lastX - size / 2, lastY - size / 2, size, size);
                } else if (shapeToggle != null && shapeToggle.isSelected() || (gradientToggle != null && gradientToggle.isSelected())) {
                    shapeStartX = lastX;
                    shapeStartY = lastY;

                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    preShapeSnapshot = currentCanvas.snapshot(params, null);

                } else if (textToggle != null && textToggle.isSelected()) {
                    String textToDraw = textInputField.getText();
                    if (textToDraw != null && !textToDraw.isEmpty()) {
                        gc.setFill(colorPicker.getValue());

                        gc.setFont(Font.font(size * 3));

                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.setTextBaseline(VPos.CENTER);

                        gc.fillText(textToDraw, lastX, lastY);
                    }
                } else {

                    gc.setStroke(colorPicker.getValue());
                    gc.setLineWidth(size);
                    gc.beginPath();
                    gc.moveTo(lastX, lastY);
                    gc.stroke();
                }
            }
        });

        canvasContainer.setOnMouseDragged(e -> {
            if (currentCanvas == null) return;
            GraphicsContext gc = currentCanvas.getGraphicsContext2D();

            if (isPanning) {
                double deltaX = e.getSceneX() - panStartX;
                double deltaY = e.getSceneY() - panStartY;

                Bounds viewport = scrollPane.getViewportBounds();
                Bounds content = zoomContainer.getBoundsInParent();

                double hRange = content.getWidth() - viewport.getWidth();
                double vRange = content.getHeight() - viewport.getHeight();

                if (hRange > 0) {
                    scrollPane.setHvalue(Math.clamp(panStartHval - (deltaX / hRange), 0, 1));
                }
                if (vRange > 0) {
                    scrollPane.setVvalue(Math.clamp(panStartVval - (deltaY / vRange), 0, 1));
                }
                return;
            }

            if (e.getButton() == MouseButton.PRIMARY && (brushToggle.isSelected() || eraserToggle.isSelected() || shapeToggle.isSelected() || gradientToggle.isSelected())) {


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
                } else if (shapeToggle != null && shapeToggle.isSelected()) {
                    gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                    if (preShapeSnapshot != null) {
                        gc.drawImage(preShapeSnapshot, 0, 0);
                    }

                    double x = Math.min(shapeStartX, currentX);
                    double y = Math.min(shapeStartY, currentY);
                    double w = Math.abs(currentX - shapeStartX);
                    double h = Math.abs(currentY - shapeStartY);

                    gc.setLineWidth(size);
                    Color color = colorPicker.getValue();
                    boolean fill = shapeFillToggle.isSelected();
                    String shapeType = shapeTypeComboBox.getValue();

                    if (fill) gc.setFill(color);
                    else gc.setStroke(color);

                    switch (shapeType) {
                        case "Rectangle" -> {
                            if (fill) gc.fillRect(x, y, w, h);
                            else gc.strokeRect(x, y, w, h);
                        }
                        case "Oval" -> {
                            if (fill) gc.fillOval(x, y, w, h);
                            else gc.strokeOval(x, y, w, h);
                        }
                        case "Line" -> {
                            gc.setStroke(color);
                            gc.strokeLine(shapeStartX, shapeStartY, currentX, currentY);
                        }
                    }
                }else if (gradientToggle != null && gradientToggle.isSelected()) {
                    gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                    if (preShapeSnapshot != null) {
                        gc.drawImage(preShapeSnapshot, 0, 0);
                    }

                    Color color1 = colorPicker.getValue();
                    Color color2 = secondaryColorPicker.getValue();

                    Stop[] stops = new Stop[]{
                            new Stop(0, color1),
                            new Stop(1, color2)
                    };

                    LinearGradient gradient = new LinearGradient(shapeStartX, shapeStartY, currentX, currentY, false, CycleMethod.NO_CYCLE, stops);

                    gc.setFill(gradient);
                    gc.fillRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                    gc.setStroke(Color.color(0.5, 0.5, 0.5, 0.5));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(shapeStartX, shapeStartY, currentX, currentY);
                } else {
                    gc.setStroke(colorPicker.getValue());
                    gc.setLineWidth(size);
                    gc.lineTo(currentX, currentY);
                    gc.stroke();

                }

                lastX = currentX;
                lastY = currentY;
            }
        });

        canvasContainer.setOnMouseReleased(e -> {

            if (isPanning) {
                isPanning = false;
                canvasContainer.setCursor(panToggle.isSelected() ? Cursor.OPEN_HAND : Cursor.NONE);
                if (!panToggle.isSelected()) brushCursor.setVisible(true);
                return;
            }


            if (e.getButton() == MouseButton.PRIMARY && currentPaintCommand != null) {
                currentPaintCommand.savePresent();
                historyService.addCommand(currentPaintCommand);
                currentPaintCommand = null;
                preShapeSnapshot = null;
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
    private void handleAddLayer(MouseEvent actionEvent) {
        if (actionEvent.getButton() == MouseButton.PRIMARY) {
            createLayer(false);
        }
        else if (actionEvent.getButton() == MouseButton.SECONDARY) {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem emptyItem = new MenuItem("Empty Layer");
            emptyItem.setOnAction(e -> createLayer(false));

            MenuItem challengeItem = new MenuItem("Challenge Layer");
            challengeItem.setOnAction(e -> createLayer(true));

            contextMenu.getItems().addAll(emptyItem, challengeItem);

            Node source = (Node) actionEvent.getSource();
            contextMenu.show(source, javafx.geometry.Side.BOTTOM, 0, 0);
        }
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

        boolean topOgVis = topCanvas.isVisible();
        double topOgOpac = topCanvas.getOpacity();
        topCanvas.setVisible(true);
        topCanvas.setOpacity(1.0);
        WritableImage topImage = topCanvas.snapshot(parameters, null);
        topCanvas.setVisible(topOgVis);
        topCanvas.setOpacity(topOgOpac);

        boolean botOgVis = bottomCanvas.isVisible();
        double botOgOpac = bottomCanvas.getOpacity();
        bottomCanvas.setVisible(true);
        bottomCanvas.setOpacity(1.0);
        WritableImage bottomImage = bottomCanvas.snapshot(parameters, null);
        bottomCanvas.setVisible(botOgVis);
        bottomCanvas.setOpacity(botOgOpac);

        Canvas tempCanvas = new Canvas(topCanvas.getWidth(), topCanvas.getHeight());
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();

        if (bottomLayer.isVisible() && bottomLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(bottomImage, 0, 0);
        }

        if (topLayer.isVisible() && topLayer.getImage() != null){
            gc.setGlobalAlpha(1.0);
            gc.drawImage(topImage, 0, 0);
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
        openFilterDialog(new GrayScaleFilter());

    }

    @FXML
    private void handleApplySepia(ActionEvent actionEvent) {
        openFilterDialog(new SepiaFilter());

    }

    @FXML
    private void handleApplySharpen(ActionEvent actionEvent) {
        openFilterDialog(new SharpFilter());
    }

    @FXML
    private void handleApplyBlur(ActionEvent actionEvent) {
        openFilterDialog(new BlurFilter());

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
    private void handleApplyContrast(ActionEvent actionEvent) {
        openFilterDialog(new ContrastFilter());
    }

    @FXML
    private void handleApplyBrightness(ActionEvent actionEvent) {
        openFilterDialog(new BrightnessFilter());
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
            Parent root = loader.load();

            PreviewController controller = loader.getController();
            controller.initData(originalImage, editedImage);

            Stage previewStage = new Stage();
            previewStage.setTitle("Before & After");
            previewStage.setScene(new Scene(root));

            previewStage.initModality(Modality.APPLICATION_MODAL);

            previewStage.show();

        } catch (IOException e) {
            e.printStackTrace();
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

    private void setupGlobalKeybinds() {
        if (canvasContainer.getScene() == null) return;
        SettingsService keys = SettingsService.getInstance();

        Map<KeyCombination, Runnable> accelerators = canvasContainer.getScene().getAccelerators();

        accelerators.clear();

        addAccelerator(accelerators, keys.getBind(Actions.SAVE), () -> {
            try { handleSave(new ActionEvent()); } catch (Exception ignored) {}
        });
        addAccelerator(accelerators, keys.getBind(Actions.EXPORT), () -> {
            try { handleExport(new ActionEvent()); } catch (Exception ignored) {}
        });

        addAccelerator(accelerators, keys.getBind(Actions.UNDO), () -> handleUndo(new ActionEvent()));
        addAccelerator(accelerators, keys.getBind(Actions.REDO), () -> handleRedo(new ActionEvent()));

        addAccelerator(accelerators, keys.getBind(Actions.ZOOM_IN), () -> {
            Bounds bounds = scrollPane.getViewportBounds();
            applyZoom(1.1, bounds.getWidth() / 2, bounds.getHeight() / 2);
        });

        addAccelerator(accelerators, keys.getBind(Actions.ZOOM_OUT), () -> {
            Bounds bounds = scrollPane.getViewportBounds();
            applyZoom(1 / 1.1, bounds.getWidth() / 2, bounds.getHeight() / 2);
        });

        addAccelerator(accelerators, keys.getBind(Actions.RESET_ZOOM), () ->
                applyZoom(1.0 / zoomContainer.getScaleX(),
                        scrollPane.getViewportBounds().getWidth() / 2,
                        scrollPane.getViewportBounds().getHeight() / 2
                ));

        addAccelerator(accelerators, keys.getBind(Actions.TOOL_BRUSH), () -> brushToggle.setSelected(true));
        addAccelerator(accelerators, keys.getBind(Actions.TOOL_ERASER), () -> eraserToggle.setSelected(true));
        addAccelerator(accelerators, keys.getBind(Actions.GRABBING), () -> panToggle.setSelected(true));

        addAccelerator(accelerators, keys.getBind(Actions.VERTICAL_FLIP), () -> handleApplyVerticalReflect(new ActionEvent()));

        addAccelerator(accelerators, keys.getBind(Actions.HORIZONTAL_FLIP), () -> handleApplyReflect(new ActionEvent()));

        addAccelerator(accelerators, keys.getBind(Actions.CHALLENGE_LAYER), () -> createLayer(true));

        addAccelerator(accelerators, keys.getBind(Actions.EMPTY_LAYER), () -> createLayer(false));
        addAccelerator(accelerators, keys.getBind(Actions.RESIZING), () -> handleResize(new ActionEvent()));
        addAccelerator(accelerators, keys.getBind(Actions.DUPLICATE), () -> handleDuplicateLayer(new ActionEvent()));



    }
    @FXML
    private void handleDuplicateLayer(ActionEvent actionEvent) {
        SpriteLayer selectedLayer = layerListView.getSelectionModel().getSelectedItem();
        int selectedIndex = layerListView.getSelectionModel().getSelectedIndex();
        if (selectedLayer == null) return;

        String newName = selectedLayer.getName() + " Copy";
        SpriteLayer duplicateLayer = new SpriteLayer(newName, currentProject.getWidth(), currentProject.getHeight());

        Canvas duplicateCanvas = new Canvas(currentProject.getWidth(), currentProject.getHeight());
        duplicateCanvas.setId(newName);

        Canvas sourceCanvas = getCanvasForLayer(selectedLayer.getName());

        if (sourceCanvas != null) {

            boolean ogVisible = sourceCanvas.isVisible();
            double ogOpacity = sourceCanvas.getOpacity();

            sourceCanvas.setVisible(true);
            sourceCanvas.setOpacity(1.0);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = sourceCanvas.snapshot(params, null);

            sourceCanvas.setVisible(ogVisible);
            sourceCanvas.setOpacity(ogOpacity);

            duplicateCanvas.getGraphicsContext2D().drawImage(snapshot, 0, 0);
            duplicateLayer.setImage(snapshot);
        }

        duplicateLayer.setVisible(selectedLayer.isVisible());
        duplicateLayer.setOpacity(selectedLayer.getOpacity());
        duplicateCanvas.setVisible(selectedLayer.isVisible());
        duplicateCanvas.setOpacity(selectedLayer.getOpacity());

        layerListModel.add(selectedIndex, duplicateLayer);
        canvasContainer.getChildren().add(duplicateCanvas);

        syncCanvasOrder();
        layerListView.getSelectionModel().select(duplicateLayer);

    }

    private void addAccelerator(Map<KeyCombination, Runnable> accelerators, KeyCombination combo, Runnable action) {
        if (combo != null) {
            accelerators.put(combo, action);
        }
    }


    @FXML
    private void handleSettings(ActionEvent actionEvent) {
        try {
            SettingsController.previousScene = canvasContainer.getScene();
            SettingsController.callback = this::setupGlobalKeybinds;
            Main.changeScene("/view/settings-view.fxml");
            handleSave(new ActionEvent());
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    private void setupManualTooltips(Parent parent) {

        List<Node> children = new ArrayList<>();
        if (parent instanceof ToolBar) {
            children.addAll(((ToolBar) parent).getItems());
        } else {
            children.addAll(parent.getChildrenUnmodifiable());
        }

        for (Node node : children) {
            if (node instanceof Control) {
                Control control = (Control) node;
                Tooltip fxmlTooltip = control.getTooltip();

                if (fxmlTooltip != null) {
                    String exactText = fxmlTooltip.getText();

                    control.setTooltip(null);

                    Tooltip manualTooltip = new Tooltip(exactText);
                    manualTooltip.setShowDelay(Duration.millis(250));
                    Tooltip.install(control, manualTooltip);
                }
            }

            if (node instanceof Parent) {
                setupManualTooltips((Parent) node);
            }
        }
    }

    private void createLayer(boolean isChallengeLayer) {
        String newName = "Layer " + (layerListModel.size() + 1);
        SpriteLayer newLayer = new SpriteLayer(newName, 800, 600);

        Canvas newCanvas = new Canvas(currentProject.getWidth(), currentProject.getHeight());
        newCanvas.setId(newName);

        if (isChallengeLayer) {
            try {
                int projectWidth = currentProject.getWidth();
                int projectHeight = currentProject.getHeight();
                newLayer = challengesService.generateChallengeLayer(projectWidth, projectHeight, newName);

                newCanvas.getGraphicsContext2D().drawImage(newLayer.getImage(), 0, 0);
            } catch(Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("ERROR");
                error.setHeaderText("Image failed to load");
                error.showAndWait();
                return;
            }
        }

        layerListModel.addFirst(newLayer);
        canvasContainer.getChildren().add(newCanvas);
        syncCanvasOrder();
        layerListView.getSelectionModel().select(newLayer);
    }

    @FXML
    private void handleResize(ActionEvent actionEvent) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Resizing");
        dialog.setHeaderText("Please enter the new dimensions.");

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 20, 10, 10));

        TextField width = new TextField();
        width.setPromptText("Width");
        TextField height = new TextField();
        height.setPromptText("Height");

        gridPane.add(new Label("Width:"), 0, 0);
        gridPane.add(width, 1, 0);
        gridPane.add(new Label("Height:"), 0, 1);
        gridPane.add(height, 1, 1);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(width::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                return new Pair<>(width.getText(), height.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            try {

                int widthNum = Integer.parseInt(pair.getKey());
                int heightNum = Integer.parseInt(pair.getValue());

                if (widthNum <= 0 || heightNum <= 0) {
                    new Alert(Alert.AlertType.ERROR, "Dimensions must be greater than zero!").showAndWait();
                    return;
                }

               resize(heightNum,widthNum);
            } catch (NumberFormatException e ){
                new Alert(Alert.AlertType.ERROR, "Please enter valid numbers!").showAndWait();
            }
        });
    }

    private void resize(int height,int width){

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        for (Node node : canvasContainer.getChildren()) {
            if (node instanceof Canvas canvas) {
                for (SpriteLayer layer : layerListModel) {
                    if (layer.getName().equals(canvas.getId())) {
                        boolean ogVis = canvas.isVisible();
                        double ogOp = canvas.getOpacity();

                        canvas.setVisible(true);
                        canvas.setOpacity(1.0);

                        layer.setImage(canvas.snapshot(params, null));

                        canvas.setVisible(ogVis);
                        canvas.setOpacity(ogOp);
                    }
                }
            }
        }

        currentProject.setWidth(width);
        currentProject.setHeight(height);
        setupWorkspaceDimensions(width, height);

        for (Node node : canvasContainer.getChildren()) {
            if (node instanceof Canvas canvas) {
                canvas.setWidth(width);
                canvas.setHeight(height);

                GraphicsContext gc = canvas.getGraphicsContext2D();

                gc.setImageSmoothing(false);
                gc.clearRect(0, 0, width, height);

                for (SpriteLayer layer : layerListModel) {
                    if (layer.getName().equals(canvas.getId()) && layer.getImage() != null) {

                        gc.drawImage(layer.getImage(), 0, 0, width, height);

                        boolean ogVis = canvas.isVisible();
                        double ogOp = canvas.getOpacity();

                        canvas.setVisible(true);
                        canvas.setOpacity(1.0);
                        layer.setImage(canvas.snapshot(params, null));

                        canvas.setVisible(ogVis);
                        canvas.setOpacity(ogOp);
                    }
                }
            }
        }

        Platform.runLater(this::fitImageToScreen);


    }

    private Project copy(Project original) {
        Project copy = new Project();
        copy.setId(original.getId());
        copy.setTitle(original.getTitle());
        copy.setChallengeId(original.getChallengeId());
        copy.setWidth(original.getWidth());
        copy.setHeight(original.getHeight());

        List<SpriteLayer> copiedLayers = new ArrayList<>();
        if (original.getLayers() != null) {
            for (SpriteLayer layer : original.getLayers()) {
                SpriteLayer layerCopy = new SpriteLayer(layer.getName(), original.getWidth(), original.getHeight());
                layerCopy.setOpacity(layer.getOpacity());
                layerCopy.setVisible(layer.isVisible());
                layerCopy.setImage(layer.getImage());
                copiedLayers.add(layerCopy);
            }
        }
        copy.setLayers(copiedLayers);
        return copy;
    }


    private void openFilterDialog(Filter baseFilter) {
        if (currentCanvas == null) return;

        if (!(baseFilter instanceof AdjustableFilter filter)) {
            historyService.addCommand(new FilterCommand(currentCanvas, baseFilter));
            return;
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage fullOriginal = currentCanvas.snapshot(params, null);

        double scale = 300.0 / Math.max(fullOriginal.getWidth(), fullOriginal.getHeight());
        int thumbW = Math.max(1, (int) (fullOriginal.getWidth() * scale));
        int thumbH = Math.max(1, (int) (fullOriginal.getHeight() * scale));

        Canvas thumbCanvas = new Canvas(thumbW, thumbH);
        thumbCanvas.getGraphicsContext2D().drawImage(fullOriginal, 0, 0, thumbW, thumbH);
        WritableImage previewOriginal = thumbCanvas.snapshot(params, null);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(filter.getName() + " Adjustment");

        ImageView beforeView = new ImageView(previewOriginal);
        ImageView afterView = new ImageView(previewOriginal);

        HBox imageBox = new HBox(15,
                new VBox(5, new Label("Before"), beforeView),
                new VBox(5, new Label("After"), afterView)
        );
        imageBox.setAlignment(Pos.CENTER);

        Slider slider = new Slider(filter.getMinIntensity(), filter.getMaxIntensity(), filter.getDefaultIntensity());
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        VBox content = new VBox(15, imageBox, new Label("Intensity:"), slider);
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(content);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            filter.setIntensity(newVal.doubleValue());

            afterView.setImage(filter.apply(previewOriginal));
        });

        filter.setIntensity(slider.getValue());
        afterView.setImage(filter.apply(previewOriginal));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.APPLY);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.orElse(false)) {
            historyService.addCommand(new FilterCommand(currentCanvas, filter));
        }

    }
}