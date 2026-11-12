
package com.omnisys.desktop.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisys.desktop.model.InventoryItem;
import com.omnisys.desktop.service.OdooConnectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Odoo 19 sistemako biltegiko stock-datuak kudeatzen ditu.
 */
public class StockDAO {

    private final OdooConnectionService odooConnectionService;
    private final ObjectMapper objectMapper;

    public StockDAO() {
        this.odooConnectionService = new OdooConnectionService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Biltegiko produktu guztien stock-informazioa lortzen du.
     *
     * @return inbentarioko produktuen zerrenda
     */
    public List<InventoryItem> getInventoryItems() {

        List<InventoryItem> inventoryItems = new ArrayList<>();

        try {

            String json = odooConnectionService.getAvailableStockJson();

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {

                JsonNode productNode = node.path("product_id");

                if (!productNode.isArray()
                        || productNode.size() < 2) {
                    continue;
                }

                int productId = productNode.get(0).asInt();
                String productName = productNode.get(1).asText("");

                double quantity = node.path("quantity").asDouble();
                double reservedQuantity =
                        node.path("reserved_quantity").asDouble();

                InventoryItem item = new InventoryItem(
                        productId,
                        productName,
                        quantity,
                        reservedQuantity
                );

                inventoryItems.add(item);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea inbentarioa kargatzean.",
                    e
            );
        }

        return inventoryItems;
    }

    /**
     * Biltegian eskuragarri dagoen stock osoa kalkulatzen du.
     *
     * @return eskuragarri dauden unitate kopurua
     */
    public double getAvailableStock() {

        return getInventoryItems()
                .stream()
                .mapToDouble(InventoryItem::getAvailableQuantity)
                .sum();
    }

    public Map<Integer, Double> getStockByProductId() {

        Map<Integer, Double> stockByProduct = new HashMap<>();

        List<InventoryItem> inventoryItems = getInventoryItems();

        for (InventoryItem item : inventoryItems) {

            stockByProduct.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Double::sum
            );
        }

        return stockByProduct;
    }

}
