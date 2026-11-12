
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.StockDAO;
import com.omnisys.desktop.model.InventoryItem;

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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Inbentario-pantailaren kontrolatzailea.
 *
 * Odoo 19 sistemako produktu eta stock-datuak erakusten ditu.
 */
public class InbentarioaController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, String> productReferenceColumn;

    @FXML
    private TableColumn<InventoryItem, String> productNameColumn;

    @FXML
    private TableColumn<InventoryItem, Double> quantityColumn;

    @FXML
    private TableColumn<InventoryItem, Double> reservedColumn;

    @FXML
    private TableColumn<InventoryItem, Double> availableColumn;

    private final StockDAO stockDAO = new StockDAO();

    @FXML
    public void initialize() {

        configureTable();

        backButton.setOnAction(event -> goBack());

        loadInventory();
    }

    private void configureTable() {

        productReferenceColumn.setCellValueFactory(
                new PropertyValueFactory<>("productReference")
        );

        productNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("productName")
        );

        quantityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                        formatQuantity(cellData.getValue().getQuantity())
                )
        );

        reservedColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                        formatQuantity(
                                cellData.getValue().getReservedQuantity()
                        )
                )
        );

        availableColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                        formatQuantity(
                                cellData.getValue().getAvailableQuantity()
                        )
                )
        );
    }

    private Double formatQuantity(double quantity) {
        return Math.round(quantity * 100.0) / 100.0;
    }

    private void loadInventory() {

        Task<List<InventoryItem>> inventoryTask = new Task<>() {

            @Override
            protected List<InventoryItem> call() {
                return stockDAO.getInventoryItems();
            }
        };

        inventoryTask.setOnSucceeded(event -> {

            inventoryTable.setItems(
                    FXCollections.observableArrayList(
                            inventoryTask.getValue()
                    )
            );
        });

        inventoryTask.setOnFailed(event -> {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea inbentarioa kargatzean");
            alert.setContentText(
                    "Ezin izan dira Odoo-ko stock-datuak eskuratu."
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(inventoryTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void goBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/DashboardView.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) backButton.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("OMNISYS DESKTOP");
            stage.setMaximized(false);
            stage.setMaximized(true);

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
