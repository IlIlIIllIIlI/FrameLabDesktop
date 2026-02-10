package com.frameLab.frameSprite;

import com.frameLab.frameSprite.controller.LoginController;
import com.frameLab.frameSprite.controller.MainPageController;
import com.frameLab.frameSprite.utils.ApiUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class Main extends Application {
    private static Stage primaryStage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:frameSprite.db");
        ApiUtils au = new ApiUtils();
        Main.primaryStage = primaryStage;
        if (au.isLogged()){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-page-view.fxml"));
            Parent root = loader.load();
            MainPageController controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        }
        primaryStage.setTitle("FrameSprite");
        primaryStage.show();
    }


    public static void changeScene(String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(Main.class.getResource(fxml)));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
}
