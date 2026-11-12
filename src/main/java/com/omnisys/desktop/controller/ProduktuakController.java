package com.omnisys.desktop.controller;

import com.omnisys.desktop.dao.ProductDAO;
import com.omnisys.desktop.model.Product;
import com.omnisys.desktop.service.ImageService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import com.omnisys.desktop.WindowManager;
import javafx.scene.layout.HBox;

public class ProduktuakController {

    @FXML
    private Button backButton;

    @FXML
    private TilePane productsContainer;

    @FXML
    private VBox loadingContainer;

    @FXML
    private ProgressIndicator loadingIndicator;

    private final ProductDAO productDAO = new ProductDAO();
    private final ImageService imageService = new ImageService();

    private final NumberFormat euroFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-ES")
            );

    @FXML
    public void initialize() {

        backButton.setOnAction(event -> goBack());

        loadProductsInBackground();
    }

    private void loadProductsInBackground() {

        loadingContainer.setVisible(true);
        loadingContainer.setManaged(true);

        productsContainer.setVisible(false);
        productsContainer.setManaged(false);

        Task<List<Product>> productTask = new Task<>() {

            @Override
            protected List<Product> call() {
                return productDAO.getAllProducts();
            }
        };

        productTask.setOnSucceeded(event -> {

            List<Product> products = productTask.getValue();

            productsContainer.getChildren().clear();

            for (Product product : products) {
                productsContainer.getChildren().add(
                        createProductCard(product)
                );
            }

            loadingContainer.setVisible(false);
            loadingContainer.setManaged(false);

            productsContainer.setVisible(true);
            productsContainer.setManaged(true);
        });

        productTask.setOnFailed(event -> {

            loadingContainer.setVisible(false);
            loadingContainer.setManaged(false);

            productsContainer.setVisible(true);
            productsContainer.setManaged(true);

            showLoadingError();
        });

        Thread loadingThread = new Thread(productTask);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    private VBox createProductCard(Product product) {

        VBox card = new VBox(12);

        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(18));
        card.setPrefWidth(235);
        card.setPrefHeight(330);

        card.setStyle(
                "-fx-background-color: #10243D;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #1C4568;" +
                        "-fx-border-radius: 12;"
        );

        ImageView imageView = new ImageView();

        imageView.setFitWidth(190);
        imageView.setFitHeight(155);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Image productImage = imageService.convertBase64ToImage(
                product.getImageBase64()
        );

        if (productImage != null) {
            imageView.setImage(productImage);
        } else {
            imageView.setImage(createPlaceholderImage());
        }

        Label nameLabel = new Label(product.getName());

        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        nameLabel.setAlignment(Pos.CENTER);

        nameLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;"
        );

        Label referenceLabel = new Label(
                product.getDefaultCode() == null
                        ? ""
                        : product.getDefaultCode()
        );

        referenceLabel.setStyle(
                "-fx-text-fill: #9BAFC5;" +
                        "-fx-font-size: 11px;"
        );

        Label priceLabel = new Label(
                euroFormat.format(product.getListPrice())
        );

        Label stockTitleLabel = new Label("Stock:");

        stockTitleLabel.setStyle(
                "-fx-text-fill: #FF9800;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        Label stockQuantityLabel = new Label(
                formatStock(product.getStockQuantity())
        );

        stockQuantityLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        HBox stockBox = new HBox(6, stockTitleLabel, stockQuantityLabel);
        stockBox.setAlignment(Pos.CENTER);

        priceLabel.setStyle(
                "-fx-text-fill: #20DDF5;" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(
                imageView,
                nameLabel,
                referenceLabel,
                priceLabel,
                stockBox
        );

        return card;
    }

    private Image createPlaceholderImage() {

        var imageResource = getClass().getResource(
                "/images/omnisys_icon.png"
        );

        if (imageResource == null) {
            return null;
        }

        return new Image(imageResource.toExternalForm());
    }

    private void goBack() {

        try {
            Stage stage = (Stage) backButton.getScene().getWindow();

            WindowManager.openWindow(
                    stage,
                    "/fxml/DashboardView.fxml",
                    "OMNISYS DESKTOP | Aginte-panela"
            );

        } catch (IOException e) {
            showNavigationError();
        }
    }

    private void showLoadingError() {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("OMNISYS DESKTOP");
        alert.setHeaderText("Ezin izan dira produktuak kargatu.");
        alert.setContentText(
                "Egiaztatu Odoo zerbitzaria eta sare-konexioa."
        );

        alert.showAndWait();
    }

    private void showNavigationError() {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("OMNISYS DESKTOP");
        alert.setHeaderText("Nabigazio-errorea");
        alert.setContentText(
                "Ezin izan da panel nagusia ireki."
        );

        alert.showAndWait();
    }

    private String formatStock(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }

        return String.format(Locale.US, "%.2f", quantity);
    }

}