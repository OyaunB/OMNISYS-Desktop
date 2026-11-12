package com.omnisys.desktop.controller;

import com.omnisys.desktop.service.AuthService;
import com.omnisys.desktop.service.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> login());
    }

    private void login() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (authService.authenticateUser(username, password)) {

            String role = authService.getUserRole(username);

            if (role == null) {
                messageLabel.setText(
                        "Errorea erabiltzailearen rola eskuratzean."
                );
                return;
            }

            // SAIOA GORDE
            SessionManager.login(username, role);

            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/DashboardView.fxml")
                );

                Scene dashboardScene = new Scene(loader.load());

                Stage stage = (Stage) loginButton.getScene().getWindow();

                stage.setScene(dashboardScene);
                stage.setTitle("OMNISYS DESKTOP");

// PANTAILA OSOAN BERRIRO MAXIMIZATU
                stage.setMaximized(false);
                stage.setMaximized(true);

            } catch (IOException e) {

                messageLabel.setText(
                        "Errorea aginte-panela irekitzean."
                );

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("OMNISYS DESKTOP");
                alert.setHeaderText("Ezin izan da saioa hasi.");
                alert.setContentText(
                        "Errore bat gertatu da aginte-panela irekitzean."
                );
                alert.showAndWait();
            }

        } else {

            messageLabel.setStyle(
                    "-fx-text-fill: #FF7070; -fx-font-weight: bold;"
            );

            messageLabel.setText(
                    "Erabiltzaile edo pasahitz okerrak"
            );
        }
    }
}