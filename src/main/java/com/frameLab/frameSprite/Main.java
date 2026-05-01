package com.frameLab.frameSprite;

import com.frameLab.frameSprite.controller.LoginController;
import com.frameLab.frameSprite.controller.MainPageController;
import com.frameLab.frameSprite.utils.ApiUtils;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class Main extends Application {
    private static Stage primaryStage;
    public static  Connection conn;
    private static HostServices hostServices;

    static {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:frameSprite.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.getIcons().add( new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icon.png"))));
        try {
            hostServices = getHostServices();
            ApiUtils au = new ApiUtils();
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
        }catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("ERROR");
            error.setHeaderText("API NOT FOUND");
            error.setContentText("Most of the app wont work please fix it, USE DEMO MODE OTHERWISE");
            error.showAndWait();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

        }
            Main.primaryStage = primaryStage;
        primaryStage.setTitle("FrameSprite");
        primaryStage.show();
    }


    public static void changeScene(String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(Main.class.getResource(fxml)));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }


    public static Connection getConnection() {
        return conn;
    }

    public static void openWebsite(String url){
       hostServices.showDocument(url);
    }
}
