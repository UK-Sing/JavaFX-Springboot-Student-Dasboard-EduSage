package com.edusage.client;

import com.edusage.client.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("EduSage FX");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        SceneManager.switchTo("login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
