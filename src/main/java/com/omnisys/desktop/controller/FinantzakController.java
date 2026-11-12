
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.ProfitDAO;
import com.omnisys.desktop.model.FinantzaSummary;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import java.time.LocalDate;

/**
 * Finantza-pantailaren kontrolatzailea.
 *
 * Odoo 19 sistemako hileko finantza-datuak erakusten ditu.
 */
public class FinantzakController {

    @FXML
    private Button backButton;

    @FXML
    private Label incomeLabel;

    @FXML
    private Label expensesLabel;

    @FXML
    private Label profitLabel;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private Label selectedPeriodLabel;

    private final ProfitDAO profitDAO = new ProfitDAO();

    @FXML
    public void initialize() {
        backButton.setOnAction(event -> goBack());

        configureFilters();
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

        monthComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setTextFill(Color.web("#20DDF5"));
                setStyle("-fx-font-weight: bold; -fx-background-color: #263D56;");
            }
        });

        yearComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setTextFill(Color.web("#20DDF5"));
                setStyle("-fx-font-weight: bold; -fx-background-color: #263D56;");
            }
        });

        monthComboBox.getSelectionModel().select(currentMonth - 1);
        yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear));

        updateSelectedPeriodLabel();

        monthComboBox.setOnAction(event -> {
            updateSelectedPeriodLabel();
            loadFinancialSummary();
        });

        yearComboBox.setOnAction(event -> {
            updateSelectedPeriodLabel();
            loadFinancialSummary();
        });

        loadFinancialSummary();
    }

    private void updateSelectedPeriodLabel() {
        String month = monthComboBox.getValue();
        Integer year = yearComboBox.getValue();

        if (month != null && year != null) {
            selectedPeriodLabel.setText(month + " " + year);
        }
    }

    private void loadFinancialSummary() {

        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
        Integer year = yearComboBox.getValue();

        if (month < 1 || year == null) {
            return;
        }

        Task<FinantzaSummary> financialTask = new Task<>() {

            @Override
            protected FinantzaSummary call() {
                return profitDAO.getFinancialSummaryByMonthYear(month, year);
            }
        };

        financialTask.setOnSucceeded(event -> {

            FinantzaSummary summary = financialTask.getValue();

            NumberFormat formatter =
                    NumberFormat.getCurrencyInstance(Locale.US);

            incomeLabel.setText(
                    formatter.format(summary.getIncome())
            );

            expensesLabel.setText(
                    formatter.format(summary.getExpenses())
            );

            profitLabel.setText(
                    formatter.format(summary.getProfit())
            );
        });

        financialTask.setOnFailed(event -> {

            incomeLabel.setText("Ez dago eskuragarri");
            expensesLabel.setText("Ez dago eskuragarri");
            profitLabel.setText("Ez dago eskuragarri");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("Errorea finantzak kargatzean");
            alert.setContentText(
                    financialTask.getException() != null
                            ? financialTask.getException().getMessage()
                            : "Ezin izan dira Odoo-ko finantza-datuak eskuratu."
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(financialTask);
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
