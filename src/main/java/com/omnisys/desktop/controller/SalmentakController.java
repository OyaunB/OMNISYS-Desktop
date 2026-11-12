
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.SaleOrderDAO;
import com.omnisys.desktop.model.SaleOrder;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import java.time.LocalDate;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import com.omnisys.desktop.WindowManager;

/**
 * Salmenta-eskaeren pantailaren kontrolatzailea.
 *
 * Odoo 19 sistemako salmenta-eskaerak kargatzen ditu.
 */
public class SalmentakController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<SaleOrder> salesTable;

    @FXML
    private TableColumn<SaleOrder, String> orderColumn;

    @FXML
    private TableColumn<SaleOrder, String> customerColumn;

    @FXML
    private TableColumn<SaleOrder, String> dateColumn;

    @FXML
    private TableColumn<SaleOrder, String> amountColumn;

    @FXML
    private TableColumn<SaleOrder, String> stateColumn;

    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;

    private final SaleOrderDAO saleOrderDAO = new SaleOrderDAO();

    @FXML
    public void initialize() {

        configureTable();
        configureFilters();

        backButton.setOnAction(event -> goBack());

        // Pantaila osora maximizatu
        Platform.runLater(() -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(true);
        });

        loadSales();
    }

    /**
     * Taularen zutabeak konfiguratzen ditu.
     */
    private void configureTable() {

        orderColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        customerColumn.setCellValueFactory(
                new PropertyValueFactory<>("partnerName")
        );

        dateColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatDate(cellData.getValue().getDateOrder())
                )
        );

        amountColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        NumberFormat.getCurrencyInstance(Locale.US)
                                .format(cellData.getValue().getAmountTotal())
                )
        );

        stateColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatState(cellData.getValue().getState())
                )
        );
    }

    private void loadSalesForSelectedPeriod() {

        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
        Integer year = yearComboBox.getValue();

        if (month < 1 || year == null) {
            return;
        }

        Task<List<SaleOrder>> salesTask = new Task<>() {
            @Override
            protected List<SaleOrder> call() {
                return saleOrderDAO.getSalesByMonthYear(month, year);
            }
        };

        salesTask.setOnSucceeded(event -> {
            salesTable.setItems(
                    FXCollections.observableArrayList(salesTask.getValue())
            );
        });

        salesTask.setOnFailed(event -> {
            Throwable error = salesTask.getException();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errorea");
            alert.setHeaderText("Ezin izan dira salmentak kargatu");
            alert.setContentText(
                    error != null ? error.getMessage() : "Errore ezezaguna"
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(salesTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Odoo-ko salmenta-eskaerak atzeko harian kargatzen ditu.
     */
    private void loadSales() {

        Task<List<SaleOrder>> salesTask = new Task<>() {

            @Override
            protected List<SaleOrder> call() {
                return saleOrderDAO.getSalesThisMonth();
            }
        };

        salesTask.setOnSucceeded(event -> {
            salesTable.setItems(
                    FXCollections.observableArrayList(
                            salesTask.getValue()
                    )
            );
        });

        salesTask.setOnFailed(event -> {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea salmentak kargatzean");
            alert.setContentText(
                    "Ezin izan dira Odoo-ko salmentak eskuratu."
            );

            alert.showAndWait();
        });

        Thread thread = new Thread(salesTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Data irakurgarri bihurtzen du.
     */
    private String formatDate(String date) {

        try {
            LocalDateTime dateTime = LocalDateTime.parse(
                    date,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );

            return dateTime.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );

        } catch (Exception e) {
            return date;
        }
    }

    /**
     * Odoo-ko egoera euskaraz erakusten du.
     */
    private String formatState(String state) {

        return switch (state) {
            case "sale" -> "Baieztatuta";
            case "done" -> "Amaituta";
            default -> state;
        };
    }

    /**
     * Aginte-panelera itzultzen da.
     */
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

    private void configureFilters() {

        String[] months = {
                "URTARRILA", "OTSAILA", "MARTXOA", "APIRILA",
                "MAIATZA", "EKAINA", "UZTAILA", "ABUZTUA",
                "IRAILA", "URRIA", "AZAROA", "ABENDUA"
        };

        monthComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setTextFill(javafx.scene.paint.Color.web("#20DDF5"));
                setStyle("-fx-font-weight: bold; -fx-background-color: #263D56;");
            }
        });

        yearComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setTextFill(javafx.scene.paint.Color.web("#20DDF5"));
                setStyle("-fx-font-weight: bold; -fx-background-color: #263D56;");
            }
        });

        monthComboBox.getItems().setAll(months);

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        for (int year = currentYear; year >= currentYear - 10; year--) {
            yearComboBox.getItems().add(year);
        }

        monthComboBox.getSelectionModel().select(currentMonth - 1);
        yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear));

        monthComboBox.setOnAction(event -> loadSalesForSelectedPeriod());
        yearComboBox.setOnAction(event -> loadSalesForSelectedPeriod());
    }

}
