
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.PurchaseOrderDAO;
import com.omnisys.desktop.model.PurchaseOrder;

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
import javafx.scene.control.ComboBox;
import java.time.LocalDate;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import com.omnisys.desktop.WindowManager;

/**
 * Erosketa-eskaeren pantailaren kontrolatzailea.
 *
 * Odoo 19 sistemako erosketa-eskaerak kargatzen ditu.
 */
public class ErosketakController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<PurchaseOrder> purchasesTable;

    @FXML
    private TableColumn<PurchaseOrder, String> orderColumn;

    @FXML
    private TableColumn<PurchaseOrder, String> supplierColumn;

    @FXML
    private TableColumn<PurchaseOrder, String> dateColumn;

    @FXML
    private TableColumn<PurchaseOrder, String> amountColumn;

    @FXML
    private TableColumn<PurchaseOrder, String> stateColumn;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private ComboBox<Integer> yearComboBox;

    private final PurchaseOrderDAO purchaseOrderDAO =
            new PurchaseOrderDAO();

    @FXML
    public void initialize() {

        configureTable();

        backButton.setOnAction(event -> goBack());

        configureFilters();
    }

    private void configureTable() {

        orderColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        supplierColumn.setCellValueFactory(
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

    private String formatState(String state) {

        return switch (state) {
            case "purchase" -> "Baieztatuta";
            case "done" -> "Amaituta";
            default -> state;
        };
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

    private void loadPurchasesForSelectedPeriod() {

        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
        Integer year = yearComboBox.getValue();

        if (month < 1 || year == null) {
            return;
        }

        Task<List<PurchaseOrder>> purchasesTask = new Task<>() {

            @Override
            protected List<PurchaseOrder> call() {
                return purchaseOrderDAO.getPurchasesByMonthYear(month, year);
            }
        };

        purchasesTask.setOnSucceeded(event -> {
            purchasesTable.setItems(
                    FXCollections.observableArrayList(purchasesTask.getValue())
            );
        });

        purchasesTask.setOnFailed(event -> {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea erosketak kargatzean");
            alert.setContentText(
                    purchasesTask.getException() != null
                            ? purchasesTask.getException().getMessage()
                            : "Ezin izan dira Odoo-ko erosketak eskuratu."
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(purchasesTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void configureFilters() {

        String[] months = {
                "URTARRILA", "OTSAILA", "MARTXOA", "APIRILA",
                "MAIATZA", "EKAINA", "UZTAILA", "ABUZTUA",
                "IRAILA", "URRIA", "AZAROA", "ABENDUA"
        };

        monthComboBox.getItems().setAll(months);

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        for (int year = currentYear; year >= currentYear - 10; year--) {
            yearComboBox.getItems().add(year);
        }

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

        monthComboBox.getSelectionModel().select(currentMonth - 1);
        yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear));

        monthComboBox.setOnAction(event -> loadPurchasesForSelectedPeriod());
        yearComboBox.setOnAction(event -> loadPurchasesForSelectedPeriod());

        loadPurchasesForSelectedPeriod();
    }

}
