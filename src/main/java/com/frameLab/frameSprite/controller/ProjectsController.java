package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProjectsController {
    private static final Logger log = LoggerFactory.getLogger(ProjectsController.class);
    @FXML
    private VBox projectsBox;
    ProjectsService projectsService;
    SessionUtils cache;

    public void initialize() throws Exception {
        this.projectsService = new ProjectsService();
        cache = SessionUtils.getInstance();
        loadProjects();
    }

    private void loadProjects() throws Exception {
        List<Project> projects = projectsService.getProjectsByUserAndChallenge(cache.getUser().getId(),cache.getChallenge().getId());
        if (projects == null|| projects.isEmpty()) {
            Label label = new Label("You don't have any projects for this challenge, start now !");
            projectsBox.getChildren().add(label);
        } else {
            for(Project project : projects){
                projectsService.loadProject(project);
                HBox projectBox = new HBox();
                projectBox.setSpacing(20.0);
                projectBox.setOnMouseClicked(e -> {
                    try {
                        handleLoad(project);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                ImageView projectView = new ImageView();
                try {
                    Image image = new Image(project.getImageUrl(), true);
                    projectView.setImage(image);
                } catch (Exception e) {
                    throw new Exception(e);
                }
                Label label = new Label(project.getTitle());

                projectBox.getChildren().addAll(label,projectView);
            }
        }
    }

    @FXML
    private void handleNew(ActionEvent actionEvent) {
        Dialog<String> dialog = new TextInputDialog("Your_Project");

        dialog.setTitle("New Project");
        dialog.setHeaderText("Start your project !");
        dialog.setContentText("Title :");

        Optional<String> result =  dialog.showAndWait();

        result.ifPresent(title -> {
            try {

                Project newProject = new Project(0,title);

                newProject.setUserId(cache.getUser().getId());
                newProject.setChallengeId(cache.getChallenge().getId());

                handleLoad(newProject);
            } catch (Exception e) {
                log.error("e: ", e);
            }
        });

    }

    private void handleLoad(Project project) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editor-view.fxml"));
        Parent root = loader.load();

        EditorController editorController = loader.getController();

        editorController.initData(project);

        Stage stage = (Stage) projectsBox.getScene().getWindow();

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
