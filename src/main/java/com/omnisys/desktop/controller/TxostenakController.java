package com.omnisys.desktop.controller;

import com.omnisys.desktop.WindowManager;
import com.omnisys.desktop.service.ReportExportService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TxostenakController {

    @FXML
    private Button backButton;

    @FXML
    private Button salesButton;

    @FXML
    private Button purchasesButton;

    @FXML
    private Button inventoryButton;

    @FXML
    private Button financialButton;

    private final ReportExportService reportExportService =
            new ReportExportService();

    /**
     * Botoien ekintzak konfiguratu.
     */
    @FXML
    public void initialize() {

        backButton.setOnAction(event -> goBack());

        salesButton.setOnAction(event ->
                startExport(
                        "OMNISYS_Salmentak.csv",
                        reportExportService::exportSalesReport
                )
        );

        purchasesButton.setOnAction(event ->
                startExport(
                        "OMNISYS_Erosketak.csv",
                        reportExportService::exportPurchasesReport
                )
        );

        inventoryButton.setOnAction(event ->
                startExport(
                        "OMNISYS_Inbentarioa.csv",
                        reportExportService::exportInventoryReport
                )
        );

        financialButton.setOnAction(event ->
                startExport(
                        "OMNISYS_Finantzak.csv",
                        reportExportService::exportFinancialReport
                )
        );
    }

    /**
     * CSV fitxategia gordetzeko kokapena aukeratu eta esportazioa hasi.
     */
    private void startExport(String fileName, CsvExporter exporter) {

        Stage stage = (Stage) backButton.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Txostena gorde");
        fileChooser.setInitialFileName(fileName);

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV fitxategiak (*.csv)", "*.csv"
                )
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile == null) {
            return;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            selectedFile = new File(
                    selectedFile.getParentFile(),
                    selectedFile.getName() + ".csv"
            );
        }

        Path destination = selectedFile.toPath();

        setButtonsDisabled(true);

        Task<Void> exportTask = new Task<>() {

            @Override
            protected Void call() throws Exception {

                exporter.export(destination);

                return null;
            }
        };

        exportTask.setOnSucceeded(event -> {

            setButtonsDisabled(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setTitle("OMNISYS DESKTOP");
            alert.setHeaderText("TXOSTENA ESKURAGARRI");
            alert.setContentText(
                    "Txostena behar bezala gorde da:\n\n"
                            + destination.toAbsolutePath()
            );

            alert.showAndWait();
        });

        exportTask.setOnFailed(event -> {

            setButtonsDisabled(false);

            Throwable error = exportTask.getException();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Esportazio-errorea");
            alert.setHeaderText("Ezin izan da txostena esportatu.");

            alert.setContentText(
                    error != null && error.getMessage() != null
                            ? error.getMessage()
                            : "Errore ezezaguna gertatu da."
            );

            alert.showAndWait();
        });

        Thread exportThread = new Thread(
                exportTask,
                "OMNISYS-Txosten-Esportazioa"
        );

        exportThread.setDaemon(true);
        exportThread.start();
    }

    /**
     * Esportazioak irauten duen bitartean botoiak desgaitu.
     */
    private void setButtonsDisabled(boolean disabled) {

        salesButton.setDisable(disabled);
        purchasesButton.setDisable(disabled);
        inventoryButton.setDisable(disabled);
        financialButton.setDisable(disabled);
    }

    /**
     * CSV esportatzailearen interfazea.
     */
    @FunctionalInterface
    private interface CsvExporter {

        void export(Path destination) throws IOException;
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
                    "OMNISYS DESKTOP | AGINTE-PANELA"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
