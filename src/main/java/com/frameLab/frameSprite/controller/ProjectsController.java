package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.dao.ProjectDAO;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.service.ProjectsService;
import com.frameLab.frameSprite.service.StorageService;
import com.frameLab.frameSprite.utils.SessionUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        List<Project> projects = projectsService.getProjectsByUserAndChallenge(cache.getUser().getId(), cache.getChallenge().getId());
        if (projects == null || projects.isEmpty()) {
            Label label = new Label("You don't have any projects for this challenge, start now !");
            projectsBox.getChildren().add(label);
        } else {
            for (Project project : projects) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/projects-item.fxml"));
                HBox projectItem = loader.load();

                ProjectsItemController itemController = loader.getController();
                itemController.initData(project, this);

                projectsBox.getChildren().add(projectItem);


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

    public void handleLoad(Project project) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editor-view.fxml"));
        Parent root = loader.load();
        EditorController editorController = loader.getController();

        editorController.initData(project);

        Stage stage = (Stage) projectsBox.getScene().getWindow();

        stage.setTitle("FrameSprite: Editor");
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void handleGoBack(ActionEvent actionEvent) throws IOException {
        if (SessionUtils.getInstance().getUser().getId() == -1) {
            Stage stage = (Stage) projectsBox.getScene().getWindow();
            stage.setTitle("Projects");
            Main.changeScene("/view/login-view.fxml");
        } else {
            try {
                Stage stage = (Stage) projectsBox.getScene().getWindow();
                stage.setTitle("FrameSprite: Home");
                Main.changeScene("/view/main-page-view.fxml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void handleImport(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Project ZIP");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));

        File zipFile = fileChooser.showOpenDialog(projectsBox.getScene().getWindow());
        if (zipFile == null) return;

        Dialog<String> dialog = new TextInputDialog("Imported_Project");
        dialog.setTitle("Import Project");
        dialog.setHeaderText("Name your imported project");
        dialog.setContentText("Title :");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            try {
                Project importedProject = new Project();
                importedProject.setTitle(title);
                importedProject.setUserId(cache.getUser().getId());
                importedProject.setChallengeId(cache.getChallenge().getId());
                importedProject.setWidth(800);
                importedProject.setHeight(600);

                ProjectDAO projectDAO = new ProjectDAO();
                projectDAO.save(importedProject);


                StorageService storageService = new StorageService();
                storageService.importProjectFromZip(zipFile, importedProject.getId());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Successful");
                alert.setHeaderText(null);
                alert.setContentText("Project has been successfully imported!");
                alert.showAndWait();


                if (projectsBox.getChildren().size() > 1) {
                    projectsBox.getChildren().remove(1, projectsBox.getChildren().size());
                }
                loadProjects();

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Import Failed");
                error.setHeaderText("Could not import the project");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        });

    }

    public void removeProjectUI(javafx.scene.Node node) {
        projectsBox.getChildren().remove(node);
    }
}
