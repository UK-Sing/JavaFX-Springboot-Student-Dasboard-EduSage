package com.edusage.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static FXMLLoader switchTo(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/fxml/" + fxmlName));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        URL css = SceneManager.class.getResource("/css/main.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
        return loader;
    }

    public static Stage getStage() {
        return primaryStage;
    }
}
