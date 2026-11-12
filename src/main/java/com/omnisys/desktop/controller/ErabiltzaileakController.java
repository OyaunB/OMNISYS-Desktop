
package com.omnisys.desktop.controller;

import com.omnisys.desktop.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.omnisys.desktop.dao.UserDAO;
import com.omnisys.desktop.model.User;

import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import com.omnisys.desktop.service.AuthService;
import javafx.scene.control.TableCell;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.omnisys.desktop.WindowManager;

public class ErabiltzaileakController {

    @FXML
    private Button newUserButton;

    @FXML
    private VBox newUserForm;

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private ComboBox<String> newUserRoleComboBox;

    @FXML
    private Button saveUserButton;

    @FXML
    private Button cancelUserButton;

    @FXML
    private Label formMessageLabel;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();

    @FXML private TableColumn<User, Void> actionsColumn;

    @FXML
    private TableColumn<User, Void> roleActionsColumn;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {

        // DASHBOARDERA ITZULI
        backButton.setOnAction(event -> goBack());

        // ADMINISTRATZAILEAREN BAIMENA EGIAZTATU
        if (!SessionManager.isAdmin()) {

            newUserButton.setDisable(true);
            return;
        }

        // ERABILTZAILE MOTAK KONFIGURATU
        newUserRoleComboBox.getItems().addAll(
                "ADMIN",
                "USER"
        );

        // TAULAREN ZUTABEAK KONFIGURATU
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        createdAtColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdAt")
        );

        // EZABATZEKO BOTOIAK KONFIGURATU
        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private final Button deleteButton = new Button("EZABATU");

            {
                deleteButton.setStyle(
                        "-fx-background-color: #D64545;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5;"
                );

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // ERABILTZAILEAREN ROLA ALDATZEKO SELECTOREA
        // ERABILTZAILEAREN ROLA ALDATZEKO SELECTOREA
        roleActionsColumn.setCellFactory(column -> new TableCell<>() {

            private final ComboBox<String> roleComboBox = new ComboBox<>();
            private boolean updating = false;

            {
                roleComboBox.getItems().addAll("ADMIN", "USER");
                roleComboBox.setMaxWidth(Double.MAX_VALUE);

                roleComboBox.setOnAction(event -> {

                    // Ez egin ezer automatikoki eguneratzen ari bada
                    if (updating || isEmpty() || getIndex() < 0
                            || getIndex() >= getTableView().getItems().size()) {
                        return;
                    }

                    User user = getTableView().getItems().get(getIndex());

                    String newRole = roleComboBox.getValue();

                    if (newRole == null || newRole.equals(user.getRole())) {
                        return;
                    }

                    boolean updated = authService.changeUserRole(
                            SessionManager.getCurrentUsername(),
                            user.getUsername(),
                            newRole
                    );

                    if (updated) {

                        loadUsers();

                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("OMNISYS");
                        success.setHeaderText("Rola behar bezala aldatu da");
                        success.setContentText(
                                user.getUsername()
                                        + " erabiltzailearen rol berria: " + newRole
                        );
                        success.showAndWait();

                    } else {

                        // Aurreko rola berrezarri, ekintza berririk sortu gabe
                        setRoleSilently(user.getRole());

                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("OMNISYS | Segurtasuna");
                        alert.setHeaderText("Ezin izan da rola aldatu");
                        alert.setContentText(
                                "Egiaztatu baimenak edo administratzaileen kopurua."
                        );
                        alert.showAndWait();
                    }
                });
            }

            private void setRoleSilently(String role) {
                updating = true;

                try {
                    roleComboBox.setValue(role);
                } finally {
                    updating = false;
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0
                        || getIndex() >= getTableView().getItems().size()) {

                    setGraphic(null);

                } else {

                    User user = getTableView().getItems().get(getIndex());

                    // Eguneratze automatikoa da, ez erabiltzailearen ekintza
                    setRoleSilently(user.getRole());

                    setGraphic(roleComboBox);
                }
            }
        });

        // ERABILTZAILEAK KARGATU
        loadUsers();

        // ERABILTZAILE BERRIAREN FORMULARIOA IREKI
        newUserButton.setOnAction(event -> {

            newUserForm.setVisible(true);
            newUserForm.setManaged(true);

        });

        // FORMULARIOA ITXI ETA GARBITU
        cancelUserButton.setOnAction(event -> {

            newUserForm.setVisible(false);
            newUserForm.setManaged(false);

            newUsernameField.clear();
            newPasswordField.clear();

            newUserRoleComboBox.getSelectionModel().clearSelection();

            formMessageLabel.setText("");

        });

        // ERABILTZAILE BERRIA SORTU
        saveUserButton.setOnAction(event -> saveNewUser());

        System.out.println(
                "OMNISYS: Erabiltzaileak pantaila kargatuta."
        );
    }


    // ERABILTZAILEAK DATU-BASEATIK KARGATU
    private void loadUsers() {

        usersTable.setItems(
                FXCollections.observableArrayList(
                        userDAO.getAllUsers()
                )
        );

    }

    private void saveNewUser() {

        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String role = newUserRoleComboBox.getValue();

        // EREMUAK EGIAZTATU
        if (username.isEmpty() || password.isEmpty() || role == null) {
            formMessageLabel.setText(
                    "Bete eremu guztiak."
            );
            return;
        }

        if (password.length() < 5) {
            formMessageLabel.setText(
                    "Pasahitzak gutxienez 5 karaktere behar ditu."
            );
            return;
        }

        // ERABILTZAILEA SORTU
        boolean created = authService.registerUser(
                SessionManager.getCurrentUsername(),
                username,
                password,
                role
        );
        if (created) {

            formMessageLabel.setText(
                    "Erabiltzailea behar bezala sortu da!"
            );

            // TAULA EGUNERATU
            loadUsers();

            // FORMULARIOA GARBITU ETA ITXI
            newUsernameField.clear();
            newPasswordField.clear();
            newUserRoleComboBox.getSelectionModel().clearSelection();

            newUserForm.setVisible(false);
            newUserForm.setManaged(false);

        } else {

            formMessageLabel.setText(
                    "Ezin izan da erabiltzailea sortu. Erabiltzailea existitzen da edo errore bat gertatu da."
            );
        }
    }

    // ERABILTZAILEA SEGURTASUNEZ EZABATU
    private void deleteUser(User user) {

        // EZABATZEA BAIEZTATU
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("OMNISYS | Erabiltzaileak");
        confirmation.setHeaderText("Erabiltzailea ezabatu");
        confirmation.setContentText(
                "Ziur zaude " + user.getUsername() +
                        " erabiltzailea ezabatu nahi duzula?"
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                boolean deleted = authService.deleteUser(
                        SessionManager.getCurrentUsername(),
                        user.getUsername()
                );

                if (deleted) {

                    loadUsers();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("OMNISYS");
                    success.setHeaderText("Erabiltzailea ezabatuta");
                    success.setContentText(
                            user.getUsername() + " behar bezala ezabatu da."
                    );
                    success.showAndWait();

                } else {

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("OMNISYS | Segurtasuna");
                    error.setHeaderText("Ezin izan da erabiltzailea ezabatu");
                    error.setContentText(
                            "Ezin da kontua ezabatu. Egiaztatu baimenak eta administratzaileen kopurua."
                    );
                    error.showAndWait();
                }
            }
        });
    }

    // DASHBOARDERA BUELTATU
    // DASHBOARDERA BUELTATU
    private void goBack() {

        try {
            Stage stage = (Stage) backButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/DashboardView.fxml",
                    "OMNISYS DESKTOP"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
