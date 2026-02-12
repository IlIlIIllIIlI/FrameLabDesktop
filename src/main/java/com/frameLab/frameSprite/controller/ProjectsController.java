package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ProjectsController {
    @FXML
    private VBox projectsBox;
    ProjectsService projectsService;
    SessionUtils cache;

    public void initialize() throws SQLException, IOException {
        this.projectsService = new ProjectsService();
        cache = SessionUtils.getInstance();
        loadProjects();
    }

    private void loadProjects() throws IOException {
        List<Project> projects = projectsService.getProjectsByUserAndChallenge(cache.getUser().getId(),cache.getChallenge().getId());
        if (projects == null|| projects.isEmpty()) {
            Label label = new Label("You don't have any projects for this challenge, start now !");
            projectsBox.getChildren().add(label);
        } else {
            for(Project project : projects){
                HBox projectBox = new HBox();
                projectBox.setSpacing(20.0);
                projectBox.setOnMouseClicked(e -> {
                    handleLoad();
                });
                ImageView projectView = new ImageView();
                File file = new File(Objects.requireNonNull(getClass().getResource(project.getImageUrl())).toString());
                Image image = new Image(file.toURI().toString());
                projectView.setImage(image);
                Label label = new Label(project.getTitle());

                projectBox.getChildren().addAll(label,projectView);
            }
        }
    }

    @FXML
    private void handleNew(ActionEvent actionEvent) {

    }

    private void handleLoad(){

    }
}
