
package com.omnisys.desktop.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisys.desktop.model.PurchaseOrder;
import com.omnisys.desktop.service.OdooConnectionService;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de recuperar pedidos de compra desde Odoo 19.
 */
public class PurchaseOrderDAO {

    private final OdooConnectionService odooConnectionService;
    private final ObjectMapper objectMapper;

    public PurchaseOrderDAO() {
        this.odooConnectionService = new OdooConnectionService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene los pedidos de compra confirmados del mes actual.
     *
     * @return lista de pedidos de compra
     */
    public List<PurchaseOrder> getPurchasesThisMonth() {

        List<PurchaseOrder> purchases = new ArrayList<>();

        try {

            String json =
                    odooConnectionService.getPurchasesThisMonthJson();

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {

                int id = node.path("id").asInt();

                String name = node.path("name").asText("");

                JsonNode partnerNode = node.path("partner_id");

                String partnerName = "";

                if (partnerNode.isArray()
                        && partnerNode.size() > 1) {

                    partnerName = partnerNode.get(1).asText("");
                }

                String dateOrder =
                        node.path("date_order").asText("");

                double amountTotal =
                        node.path("amount_total").asDouble();

                String state =
                        node.path("state").asText("");

                PurchaseOrder purchaseOrder = new PurchaseOrder(
                        id,
                        name,
                        partnerName,
                        dateOrder,
                        amountTotal,
                        state
                );

                purchases.add(purchaseOrder);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea erosketak kargatzean.",
                    e
            );
        }

        return purchases;
    }

    /**
     * Calcula el importe total de las compras del mes actual.
     *
     * @return importe total de las compras
     */
    public double getMonthlyPurchasesTotal() {

        return getPurchasesThisMonth()
                .stream()
                .mapToDouble(PurchaseOrder::getAmountTotal)
                .sum();
    }

    public List<PurchaseOrder> getPurchasesByMonthYear(int month, int year) {

        List<PurchaseOrder> purchases = new ArrayList<>();

        try {
            String json = odooConnectionService.getPurchasesByMonthYearJson(
                    month, year
            );

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {

                int id = node.path("id").asInt();
                String name = node.path("name").asText("");

                JsonNode partnerNode = node.path("partner_id");
                String partnerName = "";

                if (partnerNode.isArray() && partnerNode.size() > 1) {
                    partnerName = partnerNode.get(1).asText("");
                }

                String dateOrder = node.path("date_order").asText("");
                double amountTotal = node.path("amount_total").asDouble();
                String state = node.path("state").asText("");

                purchases.add(new PurchaseOrder(
                        id,
                        name,
                        partnerName,
                        dateOrder,
                        amountTotal,
                        state
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "OMNISYS: Errorea erosketak kargatzean.", e
            );
        }

        return purchases;
    }

    public Map<YearMonth, Double> getPurchasesLastSixMonths() {
        Map<YearMonth, Double> monthlyPurchases = new LinkedHashMap<>();

        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);

            List<PurchaseOrder> purchases = getPurchasesByMonthYear(
                    month.getMonthValue(),
                    month.getYear()
            );

            double total = purchases.stream()
                    .mapToDouble(PurchaseOrder::getAmountTotal)
                    .sum();

            monthlyPurchases.put(month, total);
        }

        return monthlyPurchases;
    }

}
