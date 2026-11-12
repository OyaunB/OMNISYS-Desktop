package com.omnisys.desktop;

import com.omnisys.desktop.service.AuthService;
import com.omnisys.desktop.service.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de OMNISYS DESKTOP.
 *
 * Se encarga de inicializar la aplicación,
 * preparar la base de datos y cargar la pantalla de inicio.
 */
public class MainApp extends Application {

    /**
     * Inicializa la aplicación JavaFX.
     *
     * @param primaryStage ventana principal de la aplicación
     * @throws IOException si no se puede cargar la pantalla inicial
     */
    @Override
    public void start(Stage primaryStage) throws IOException {

        // DATU-BASEA HASIERATU
        DatabaseManager.initializeDatabase();

        // HASIERAKO ADMINISTRATZAILEA SORTU
        AuthService authService = new AuthService();

        if (!authService.userExists("admin")) {

            authService.createInitialAdmin(
                    "admin",
                    "admin"
            );
        }

        // HASIERAKO SAIOA
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("OMNISYS DESKTOP");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Abiarazten du JavaFX aplikazioa.
     *
     * @param args aplikazioaren abiarazte-argumentuak
     */
    public static void main(String[] args) {
        launch(args);
    }
}