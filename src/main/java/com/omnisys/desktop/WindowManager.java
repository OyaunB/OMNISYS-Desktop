package com.omnisys.desktop;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Leiho guztien kudeaketa zentralizatua.
 */
public class WindowManager {

    private WindowManager() {
        // Ez da instantziarik sortuko.
    }

    public static void openWindow(
            Stage stage,
            String fxmlPath,
            String title
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                WindowManager.class.getResource(fxmlPath)
        );

        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.setTitle(title);

        // Leihoa erakutsi eta maximizazioa behartu.
        stage.show();
        stage.setMaximized(false);

        Platform.runLater(() -> stage.setMaximized(true));
    }
}