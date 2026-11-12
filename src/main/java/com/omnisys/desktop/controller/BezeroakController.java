
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.CustomerDAO;
import com.omnisys.desktop.model.Customer;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.omnisys.desktop.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Bezeroen pantailaren kontrolatzailea.
 *
 * Odoo 19 sistemako bezeroak kargatzen ditu.
 */
public class BezeroakController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {

        configureTable();

        backButton.setOnAction(event -> goBack());

        loadCustomers();
    }

    private void configureTable() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );
    }

    private void loadCustomers() {

        Task<List<Customer>> customersTask = new Task<>() {

            @Override
            protected List<Customer> call() {
                return customerDAO.getAllCustomers();
            }
        };

        customersTask.setOnSucceeded(event -> {
            customersTable.setItems(
                    FXCollections.observableArrayList(
                            customersTask.getValue()
                    )
            );
        });

        customersTask.setOnFailed(event -> {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea bezeroak kargatzean");
            alert.setContentText(
                    "Ezin izan dira Odoo-ko bezeroak eskuratu."
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(customersTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void goBack() {

        try {
            Stage stage = (Stage) backButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/DashboardView.fxml",
                    "OMNISYS DESKTOP"
            );

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea");
            alert.setContentText(
                    "Ezin izan da aginte-panela ireki."
            );
            alert.showAndWait();
        }
    }
}
