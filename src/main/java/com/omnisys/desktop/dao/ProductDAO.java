
package com.omnisys.desktop.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisys.desktop.model.Product;
import com.omnisys.desktop.service.OdooConnectionService;

import java.util.ArrayList;
import java.util.List;
import com.omnisys.desktop.dao.StockDAO;
import java.util.Map;

/**
 * DAO encargado de recuperar productos desde Odoo 19.
 */
public class ProductDAO {

    private final OdooConnectionService odooConnectionService;
    private final ObjectMapper objectMapper;
    private final StockDAO stockDAO = new StockDAO();

    public ProductDAO() {
        this.odooConnectionService = new OdooConnectionService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene los productos activos de Odoo, incluyendo sus imágenes.
     *
     * @return lista de productos
     */
    public List<Product> getAllProducts() {

        List<Product> products = new ArrayList<>();

        Map<Integer, Double> stockByProduct =
                stockDAO.getStockByProductId();

        try {
            String json = odooConnectionService.getProductsJson();

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {

                int id = node.path("id").asInt();

                String name = node.path("name").asText("");

                String defaultCode = node.path("default_code").isNull()
                        || node.path("default_code").isMissingNode()
                        ? ""
                        : node.path("default_code").asText("");

                double listPrice = node.path("list_price").asDouble();

                JsonNode imageNode = node.path("image_512");

                String imageBase64 = imageNode.isMissingNode()
                        || imageNode.isNull()
                        || imageNode.isBoolean()
                        ? null
                        : imageNode.asText();

                double stockQuantity = stockByProduct.getOrDefault(id, 0.0);

                Product product = new Product(
                        id,
                        name,
                        defaultCode,
                        listPrice,
                        imageBase64,
                        stockQuantity
                );

                products.add(product);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "OMNISYS: Errorea Odoo-ko produktuak kargatzean.",
                    e
            );
        }

        return products;
    }

    /**
     * Cuenta los productos activos registrados en Odoo.
     *
     * @return número total de productos activos
     */
    public int getTotalActiveProducts() {

        try {

            String json =
                    odooConnectionService.getActiveProductsJson();

            JsonNode root = objectMapper.readTree(json);

            return root.size();

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea produktu kopurua kalkulatzean.",
                    e
            );
        }
    }

}
