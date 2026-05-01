package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.ProjectsService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class ProjectsItemController {
    @FXML
    private Button deleteBtn;
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView thumbnailView;
    private ProjectsController mainController;
    private ProjectsService projectsService;
    private Project project;
    @FXML
    public void initialize() {


    }
    public void initData(Project project, ProjectsController mainController) {
        this.project = project;
        this.mainController = mainController;
        this.projectsService = new ProjectsService();

        titleLabel.setText(project.getTitle());
         Image image = new Image(project.getImageUrl(), true);
            thumbnailView.setImage(image);

    }


    @FXML
    private void handleLoad(MouseEvent mouseEvent) throws IOException {
        projectsService.loadProject(project);
        mainController.handleLoad(project);
    }

    @FXML
    private  void handleDelete(ActionEvent actionEvent) {
        actionEvent.consume();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Project");
        confirm.setHeaderText("Are you sure you want to delete '" + project.getTitle() + "'?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    projectsService.deleteProject(project);
                    mainController.removeProjectUI(deleteBtn.getParent());
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to delete project", ex);
                }
            }
        });
    }

}
