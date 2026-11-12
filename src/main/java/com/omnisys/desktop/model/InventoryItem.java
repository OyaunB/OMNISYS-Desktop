
package com.omnisys.desktop.model;

/**
 * Biltegiko produktu baten stock-informazioa gordetzen du.
 */
public class InventoryItem {

    private final int productId;
    private final String productReference;
    private final String productName;
    private final double quantity;
    private final double reservedQuantity;
    private final double availableQuantity;

    public InventoryItem(
            int productId,
            String productName,
            double quantity,
            double reservedQuantity
    ) {
        this.productId = productId;

        // PRODUKTUAREN ERREFERENTZIA ETA IZENA BEREIZTU
        String reference = "";
        String name = productName;

        if (productName != null
                && productName.startsWith("[")
                && productName.contains("]")) {

            int endIndex = productName.indexOf("]");

            reference = productName.substring(1, endIndex).trim();
            name = productName.substring(endIndex + 1).trim();
        }

        this.productReference = reference;
        this.productName = name;

        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = quantity - reservedQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductReference() {
        return productReference;
    }

    public String getProductName() {
        return productName;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getReservedQuantity() {
        return reservedQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }
}
