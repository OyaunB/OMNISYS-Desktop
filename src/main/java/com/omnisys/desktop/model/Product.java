package com.omnisys.desktop.model;


/**
 * Representa un producto del catálogo de Odoo 19.
 *
 * Contiene la información básica del producto:
 * identificador, nombre, referencia, precio e imagen.
 */
public class Product {

    private final int id;
    private final String name;
    private final String defaultCode;
    private final double listPrice;
    private final String imageBase64;
    private final double stockQuantity;

    /**
     * Crea un nuevo producto.
     *
     * @param id identificador del producto en Odoo
     * @param name nombre del producto
     * @param defaultCode referencia interna del producto
     * @param listPrice precio de venta
     * @param imageBase64 imagen del producto codificada en Base64
     */
    public Product(int id, String name, String defaultCode,
                   double listPrice, String imageBase64,
                   double stockQuantity) {

        this.id = id;
        this.name = name;
        this.defaultCode = defaultCode;
        this.listPrice = listPrice;
        this.imageBase64 = imageBase64;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefaultCode() {
        return defaultCode;
    }

    public double getListPrice() {
        return listPrice;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }
}