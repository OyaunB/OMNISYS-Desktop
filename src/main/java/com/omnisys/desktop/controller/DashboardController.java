
package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.SaleOrderDAO;
import com.omnisys.desktop.service.AuthService;
import com.omnisys.desktop.service.SessionManager;

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
import java.time.LocalDate;
import com.omnisys.desktop.dao.PurchaseOrderDAO;
import com.omnisys.desktop.dao.CustomerDAO;
import com.omnisys.desktop.dao.ProductDAO;
import com.omnisys.desktop.dao.StockDAO;
import com.omnisys.desktop.dao.ProfitDAO;
import com.omnisys.desktop.WindowManager;
import com.omnisys.desktop.model.MonthlyChartData;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OMNISYS DESKTOP-eko aginte-panelaren kontrolatzailea.
 */
public class DashboardController {

    @FXML
    private Button usersButton;

    @FXML
    private Button productsButton;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label monthlySalesLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Label monthlySalesMonthLabel;

    @FXML
    private Label monthlyPurchasesLabel;

    @FXML
    private Label monthlyPurchasesMonthLabel;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalStockLabel;

    @FXML
    private Label profitMonthLabel;

    @FXML
    private Label profitValueLabel;

    @FXML
    private Button salesButton;

    @FXML
    private Button purchasesButton;

    @FXML
    private Button customersButton;

    @FXML
    private Button financesButton;

    @FXML
    private Button inventoryButton;

    @FXML
    private BarChart<String, Number> salesPurchasesChart;

    @FXML
    private LineChart<String, Number> salesEvolutionChart;

    @FXML
    private Button reportsButton;

    private final ProductDAO productDAO = new ProductDAO();

    private final CustomerDAO customerDAO = new CustomerDAO();

    private final PurchaseOrderDAO purchaseOrderDAO =
            new PurchaseOrderDAO();

    private final AuthService authService = new AuthService();

    private final SaleOrderDAO saleOrderDAO = new SaleOrderDAO();

    private final StockDAO stockDAO = new StockDAO();

    private final ProfitDAO profitDAO = new ProfitDAO();

    @FXML
    public void initialize() {

        // ERABILTZAILEAREN ROLA DATU-BASEAN EGUNERATU
        String username = SessionManager.getCurrentUsername();

        if (username == null) {
            logout();
            return;
        }

        String updatedRole = authService.getUserRole(username);

        if (updatedRole == null) {
            logout();
            return;
        }

        SessionManager.login(username, updatedRole);

        currentUserLabel.setText(
                username.toLowerCase()
                        + "  |  "
                        + updatedRole.toUpperCase()
        );

        // ADMINISTRATZAILEENTZAKO SARBIDEA
        usersButton.setVisible(SessionManager.isAdmin());
        usersButton.setManaged(SessionManager.isAdmin());

        if (SessionManager.isAdmin()) {
            usersButton.setOnAction(event -> openUsersView());
        }

        logoutButton.setOnAction(event -> logout());
        productsButton.setOnAction(event -> openProductsView());


        // UNEKO HILABETEA ERAKUTSI
        String[] months = {
                "URTARRILA", "OTSAILA", "MARTXOA",
                "APIRILA", "MAIATZA", "EKAINA",
                "UZTAILA", "ABUZTUA", "IRAILA",
                "URRIA", "AZAROA", "ABENDUA"
        };

        int monthIndex = LocalDate.now().getMonthValue() - 1;

        monthlySalesMonthLabel.setText(months[monthIndex]);
        profitMonthLabel.setText(months[monthIndex]);

        // FUNTZIOAK KARGATU
        loadMonthlySales();
        loadMonthlyPurchases();
        loadTotalCustomers();
        loadTotalProducts();
        loadTotalStock();
        loadMonthlyProfit();
        loadMonthlyCharts();
        salesButton.setOnAction(event -> openSalesView());
        purchasesButton.setOnAction(event -> openPurchasesView());
        customersButton.setOnAction(event -> openCustomersView());
        financesButton.setOnAction(event -> openFinancesView());
        reportsButton.setOnAction(event -> openReportsView());
        inventoryButton.setOnAction(event -> openInventoryView());
    }

    /**
     * Azken sei hilabeteetako salmentak eta erosketak grafikoetan erakusten ditu.
     */
    private void loadMonthlyCharts() {

        Task<List<MonthlyChartData>> chartTask = new Task<>() {

            @Override
            protected List<MonthlyChartData> call() {

                Map<YearMonth, Double> sales =
                        saleOrderDAO.getSalesLastSixMonths();

                Map<YearMonth, Double> purchases =
                        purchaseOrderDAO.getPurchasesLastSixMonths();

                List<MonthlyChartData> chartData = new ArrayList<>();

                YearMonth currentMonth = YearMonth.now();

                for (int i = 5; i >= 0; i--) {

                    YearMonth month = currentMonth.minusMonths(i);

                    double salesTotal = sales.getOrDefault(month, 0.0);
                    double purchasesTotal = purchases.getOrDefault(month, 0.0);

                    chartData.add(
                            new MonthlyChartData(
                                    month,
                                    salesTotal,
                                    purchasesTotal
                            )
                    );
                }

                return chartData;
            }
        };

        chartTask.setOnSucceeded(event -> {

            List<MonthlyChartData> data = chartTask.getValue();

            // SALMENTAK ETA EROSKETAK - BARRA GRAFIKOA

            XYChart.Series<String, Number> salesSeries =
                    new XYChart.Series<>();

            salesSeries.setName("Salmentak");

            XYChart.Series<String, Number> purchasesSeries =
                    new XYChart.Series<>();

            purchasesSeries.setName("Erosketak");

            // SALMENTEN BILAKAERA - LERRO GRAFIKOA

            XYChart.Series<String, Number> evolutionSeries =
                    new XYChart.Series<>();

            evolutionSeries.setName("Salmentak");

            for (MonthlyChartData monthData : data) {

                String monthName = monthData.getMonthName();

                salesSeries.getData().add(
                        new XYChart.Data<>(
                                monthName,
                                monthData.getSales()
                        )
                );

                purchasesSeries.getData().add(
                        new XYChart.Data<>(
                                monthName,
                                monthData.getPurchases()
                        )
                );

                evolutionSeries.getData().add(
                        new XYChart.Data<>(
                                monthName,
                                monthData.getSales()
                        )
                );
            }

            salesPurchasesChart.getData().clear();

            salesPurchasesChart.getData().addAll(
                    salesSeries,
                    purchasesSeries
            );

            salesEvolutionChart.getData().clear();

            salesEvolutionChart.getData().add(evolutionSeries);
        });

        chartTask.setOnFailed(event -> {

            System.err.println(
                    "OMNISYS: ERROREA GRAFIKOAK KARGATZEAN"
            );

            chartTask.getException().printStackTrace();
        });

        Thread chartThread = new Thread(chartTask);

        chartThread.setDaemon(true);
        chartThread.start();
    }

    /**
     * Odoo-ko hilabeteko salmentak kargatzen ditu.
     */
    private void loadMonthlySales() {

        Task<Double> salesTask = new Task<>() {

            @Override
            protected Double call() {
                return saleOrderDAO.getMonthlySalesTotal();
            }
        };

        salesTask.setOnSucceeded(event -> {

            double total = salesTask.getValue();

            NumberFormat formatter =
                    NumberFormat.getNumberInstance(
                            Locale.forLanguageTag("es-ES")
                    );

            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            monthlySalesLabel.setText(
                    formatter.format(total) + " €"
            );
        });

        salesTask.setOnFailed(event -> {
            monthlySalesLabel.setText("Ez dago eskuragarri");
        });

        Thread salesThread = new Thread(salesTask);

        salesThread.setDaemon(true);
        salesThread.start();
    }

    /**
     * Odoo-ko hilabeteko erosketak kargatzen ditu.
     */
    private void loadMonthlyPurchases() {

        Task<Double> purchasesTask = new Task<>() {

            @Override
            protected Double call() {
                return purchaseOrderDAO.getMonthlyPurchasesTotal();
            }
        };

        purchasesTask.setOnSucceeded(event -> {

            double total = purchasesTask.getValue();

            NumberFormat formatter =
                    NumberFormat.getNumberInstance(
                            Locale.forLanguageTag("es-ES")
                    );

            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            monthlyPurchasesLabel.setText(
                    formatter.format(total) + " €"
            );
        });

        purchasesTask.setOnFailed(event -> {

            monthlyPurchasesLabel.setText("Ez dago eskuragarri");

            System.err.println(
                    "OMNISYS: ERROR AL CARGAR LAS COMPRAS"
            );

            purchasesTask.getException().printStackTrace();
        });

        Thread purchasesThread = new Thread(purchasesTask);

        purchasesThread.setDaemon(true);
        purchasesThread.start();

        // UNEKO HILABETEA EROSKETETAN ERAKUTSI
        String[] months = {
                "URTARRILA", "OTSAILA", "MARTXOA",
                "APIRILA", "MAIATZA", "EKAINA",
                "UZTAILA", "ABUZTUA", "IRAILA",
                "URRIA", "AZAROA", "ABENDUA"
        };

        int monthIndex = LocalDate.now().getMonthValue() - 1;

        monthlyPurchasesMonthLabel.setText(months[monthIndex]);
    }

    /**
     * Odoo-ko bezero kopurua kargatzen du.
     */
    private void loadTotalCustomers() {

        Task<Integer> customersTask = new Task<>() {

            @Override
            protected Integer call() {
                return customerDAO.getTotalCustomers();
            }
        };

        customersTask.setOnSucceeded(event -> {

            totalCustomersLabel.setText(
                    String.valueOf(customersTask.getValue())
            );
        });

        customersTask.setOnFailed(event -> {

            totalCustomersLabel.setText("Ez dago eskuragarri");

            System.err.println(
                    "OMNISYS: ERROREA BEZEROAK KARGATZEAN"
            );

            customersTask.getException().printStackTrace();
        });

        Thread customersThread = new Thread(customersTask);

        customersThread.setDaemon(true);
        customersThread.start();
    }

    /**
     * Odoo-ko produktu kopurua kargatzen du.
     */
    private void loadTotalProducts() {

        Task<Integer> productsTask = new Task<>() {

            @Override
            protected Integer call() {
                return productDAO.getTotalActiveProducts();
            }
        };

        productsTask.setOnSucceeded(event -> {

            totalProductsLabel.setText(
                    String.valueOf(productsTask.getValue())
            );
        });

        productsTask.setOnFailed(event -> {

            totalProductsLabel.setText("Ez dago eskuragarri");

            System.err.println(
                    "OMNISYS: ERROR AL CARGAR LOS PRODUCTOS"
            );

            productsTask.getException().printStackTrace();
        });

        Thread productsThread = new Thread(productsTask);

        productsThread.setDaemon(true);
        productsThread.start();
    }

    /**
     * Odoo-ko biltegiko stock eskuragarria kargatzen du.
     */
    private void loadTotalStock() {

        Task<Double> stockTask = new Task<>() {

            @Override
            protected Double call() {
                return stockDAO.getAvailableStock();
            }
        };

        stockTask.setOnSucceeded(event -> {

            NumberFormat formatter =
                    NumberFormat.getNumberInstance(
                            Locale.forLanguageTag("es-ES")
                    );

            formatter.setMaximumFractionDigits(2);

            totalStockLabel.setText(
                    formatter.format(stockTask.getValue())
            );
        });

        stockTask.setOnFailed(event -> {
            totalStockLabel.setText("Ez dago eskuragarri");
        });

        Thread stockThread = new Thread(stockTask);
        stockThread.setDaemon(true);
        stockThread.start();
    }

    /**
     * Hilabete honetako irabaziak kargatzen ditu.
     */
    private void loadMonthlyProfit() {

        Task<Double> profitTask = new Task<>() {

            @Override
            protected Double call() {
                return profitDAO.getMonthlyProfit();
            }
        };

        profitTask.setOnSucceeded(event -> {

            NumberFormat formatter =
                    NumberFormat.getCurrencyInstance(Locale.US);

            profitValueLabel.setText(
                    formatter.format(profitTask.getValue())
            );
        });

        profitTask.setOnFailed(event -> {
            profitValueLabel.setText("Ez dago eskuragarri");
        });

        Thread profitThread = new Thread(profitTask);
        profitThread.setDaemon(true);
        profitThread.start();
    }

    private void openUsersView() {
        try {
            Stage stage = (Stage) usersButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/ErabiltzaileakView.fxml",
                    "OMNISYS DESKTOP | ERABILTZAILEAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void logout() {

        // UNEKO SAIOA ITXI
        SessionManager.logout();

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/LoginView.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) logoutButton.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("OMNISYS DESKTOP");

            // PANTAILA OSOAN ERAKUTSI
            stage.setMaximized(false);
            stage.setMaximized(true);

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void openProductsView() {
        try {
            Stage stage = (Stage) productsButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/ProduktuakView.fxml",
                    "OMNISYS DESKTOP | PRODUKTUAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void showNavigationError() {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("OMNISYS DESKTOP");
        alert.setHeaderText("Nabigazio-errorea");
        alert.setContentText(
                "Ezin izan da eskatutako leihoa ireki."
        );

        alert.showAndWait();
    }

    private void openSalesView() {

        try {

            Stage stage = (Stage) salesButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/SalmentakView.fxml",
                    "OMNISYS DESKTOP | SALMENTAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    //Erosketa funtzioa
    private void openPurchasesView() {
        try {
            Stage stage = (Stage) purchasesButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/ErosketakView.fxml",
                    "OMNISYS DESKTOP | EROSKETAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void openCustomersView() {
        try {
            Stage stage = (Stage) customersButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/BezeroakView.fxml",
                    "OMNISYS DESKTOP | BEZEROAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void openFinancesView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FinantzakView.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) financesButton.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("OMNISYS DESKTOP | FINANTZAK");
            stage.setMaximized(false);
            stage.setMaximized(true);

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void openInventoryView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/InbentarioaView.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) inventoryButton.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("OMNISYS DESKTOP | INBENTARIOA");
            stage.setMaximized(false);
            stage.setMaximized(true);

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void openReportsView() {

        try {

            Stage stage = (Stage) reportsButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/TxostenakView.fxml",
                    "OMNISYS DESKTOP | TXOSTENAK"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

}
