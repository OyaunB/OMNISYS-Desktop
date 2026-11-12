
package com.omnisys.desktop.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisys.desktop.model.SaleOrder;
import com.omnisys.desktop.service.OdooConnectionService;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de recuperar pedidos de venta desde Odoo 19.
 */
public class SaleOrderDAO {

    private final OdooConnectionService odooConnectionService;
    private final ObjectMapper objectMapper;

    public SaleOrderDAO() {
        this.odooConnectionService = new OdooConnectionService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene los pedidos confirmados del mes actual.
     *
     * @return lista de pedidos de venta
     */
    public List<SaleOrder> getSalesThisMonth() {

        List<SaleOrder> sales = new ArrayList<>();

        try {

            String json =
                    odooConnectionService.getSalesThisMonthJson();

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

                SaleOrder saleOrder = new SaleOrder(
                        id,
                        name,
                        partnerName,
                        dateOrder,
                        amountTotal,
                        state
                );

                sales.add(saleOrder);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea salmentak kargatzean.",
                    e
            );
        }

        return sales;
    }

    /**
     * Hautatutako hilabeteko eta urteko salmentak lortzen ditu.
     */
    public List<SaleOrder> getSalesByMonthYear(int month, int year) {

        List<SaleOrder> sales = new ArrayList<>();

        try {

            String json =
                    odooConnectionService.getSalesByMonthYearJson(month, year);

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

                sales.add(new SaleOrder(
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
                    "OMNISYS: Errorea salmentak kargatzean.",
                    e
            );
        }

        return sales;
    }

    /**
     * Calcula el importe total de las ventas del mes actual.
     *
     * @return importe total de las ventas
     */
    public double getMonthlySalesTotal() {

        return getSalesThisMonth()
                .stream()
                .mapToDouble(SaleOrder::getAmountTotal)
                .sum();
    }

    public Map<YearMonth, Double> getSalesLastSixMonths() {
        Map<YearMonth, Double> monthlySales = new LinkedHashMap<>();

        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);

            List<SaleOrder> sales = getSalesByMonthYear(
                    month.getMonthValue(),
                    month.getYear()
            );

            double total = sales.stream()
                    .mapToDouble(SaleOrder::getAmountTotal)
                    .sum();

            monthlySales.put(month, total);
        }

        return monthlySales;
    }

}
