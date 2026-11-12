package com.omnisys.desktop.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.ArrayList;

/**
 * CSV txostenak sortu eta esportatzeko zerbitzua.
 */
public class ReportExportService {

    private static final String SEPARATOR = ";";

    /**
     * CSV fitxategi bat sortzen du Excel-ekin bateragarria den formatuan.
     *
     * @param destination Helmugako fitxategiaren kokapena
     * @param headers Zutabeen izenak
     * @param rows Esportatu beharreko datuak
     * @throws IOException Fitxategia idaztean errorea gertatzen bada
     */
    public void exportCsv(
            Path destination,
            List<String> headers,
            List<List<String>> rows
    ) throws IOException {

        if (destination == null) {
            throw new IllegalArgumentException(
                    "Ez da fitxategiaren helmuga zehaztu."
            );
        }

        if (headers == null || rows == null) {
            throw new IllegalArgumentException(
                    "Txostenaren datuak ezin dira hutsik egon."
            );
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                destination,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {

            // UTF-8 BOM: Excel-en karaktere bereziak ondo erakusteko.
            writer.write('\uFEFF');

            writeRow(writer, headers);

            for (List<String> row : rows) {
                writeRow(writer, row);
            }
        }
    }

    /**
     * CSV-ko errenkada bat idazten du.
     */
    private void writeRow(
            BufferedWriter writer,
            List<String> values
    ) throws IOException {

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                writer.write(SEPARATOR);
            }

            String value = values.get(i);

            if (value == null) {
                value = "";
            }

            // Komatxo bikoitzak bikoiztu eta gelaxka babestu.
            writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            writer.write('"');
        }

        writer.newLine();
    }


    /**
     * Odoo-ko salmenten txostena CSV formatuan esportatzen du.
     *
     * @param destination CSV fitxategiaren helmuga
     * @throws IOException Esportazioan errorea gertatzen bada
     */
    public void exportSalesReport(Path destination) throws IOException {

        OdooConnectionService odoo = new OdooConnectionService();

        String json = odoo.getAllSalesForReportJson();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode sales = mapper.readTree(json);

        List<List<String>> rows = new ArrayList<>();

        for (JsonNode sale : sales) {

            JsonNode partner = sale.path("partner_id");

            String customerName = "";

            if (partner.isArray() && partner.size() > 1) {
                customerName = partner.get(1).asText("");
            } else {
                customerName = partner.asText("");
            }

            rows.add(List.of(
                    sale.path("name").asText(""),
                    customerName,
                    sale.path("date_order").asText(""),
                    sale.path("amount_total").asText("0"),
                    sale.path("state").asText("")
            ));
        }

        List<String> headers = List.of(
                "ERREFERENTZIA",
                "BEZEROA",
                "DATA",
                "ZENBATEKOA",
                "EGOERA"
        );

        exportCsv(destination, headers, rows);
    }


    /**
     * Odoo-ko erosketa-txostena CSV formatuan esportatzen du.
     *
     * @param destination CSV fitxategiaren helmuga
     * @throws IOException Esportazioan errorea gertatzen bada
     */
    public void exportPurchasesReport(Path destination) throws IOException {

        OdooConnectionService odoo = new OdooConnectionService();

        String json = odoo.getAllPurchasesForReportJson();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode purchases = mapper.readTree(json);

        List<List<String>> rows = new ArrayList<>();

        for (JsonNode purchase : purchases) {

            JsonNode partner = purchase.path("partner_id");

            String supplierName = "";

            if (partner.isArray() && partner.size() > 1) {
                supplierName = partner.get(1).asText("");
            } else {
                supplierName = partner.asText("");
            }

            rows.add(List.of(
                    purchase.path("name").asText(""),
                    supplierName,
                    purchase.path("date_order").asText(""),
                    purchase.path("amount_total").asText("0"),
                    purchase.path("state").asText("")
            ));
        }

        List<String> headers = List.of(
                "ERREFERENTZIA",
                "HORNITZAILEA",
                "DATA",
                "ZENBATEKOA",
                "EGOERA"
        );

        exportCsv(destination, headers, rows);
    }


    /**
     * Odoo-ko inbentario-txostena CSV formatuan esportatzen du.
     *
     * Produktu bakoitzaren stocka eta erreserbak batzen ditu.
     *
     * @param destination CSV fitxategiaren helmuga
     * @throws IOException Esportazioan errorea gertatzen bada
     */
    public void exportInventoryReport(Path destination) throws IOException {

        OdooConnectionService odoo = new OdooConnectionService();

        String json = odoo.getAvailableStockJson();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode inventory = mapper.readTree(json);

        Map<String, String> productNames = new LinkedHashMap<>();
        Map<String, BigDecimal[]> totals = new LinkedHashMap<>();

        for (JsonNode item : inventory) {

            JsonNode product = item.path("product_id");

            String productId = "";
            String productName = "";

            if (product.isArray() && product.size() > 1) {
                productId = product.get(0).asText("");
                productName = product.get(1).asText("");
            }

            if (productId.isBlank()) {
                continue;
            }

            BigDecimal quantity = item.path("quantity").decimalValue();
            BigDecimal reserved = item.path("reserved_quantity").decimalValue();

            productNames.put(productId, productName);

            BigDecimal[] values = totals.computeIfAbsent(
                    productId,
                    key -> new BigDecimal[]{
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    }
            );

            values[0] = values[0].add(quantity);
            values[1] = values[1].add(reserved);
        }

        List<List<String>> rows = new ArrayList<>();

        for (Map.Entry<String, BigDecimal[]> entry : totals.entrySet()) {

            String productName = productNames.get(entry.getKey());

            BigDecimal quantity = entry.getValue()[0];
            BigDecimal reserved = entry.getValue()[1];
            BigDecimal available = quantity.subtract(reserved);

            rows.add(List.of(
                    productName,
                    quantity.stripTrailingZeros().toPlainString(),
                    reserved.stripTrailingZeros().toPlainString(),
                    available.stripTrailingZeros().toPlainString()
            ));
        }

        List<String> headers = List.of(
                "PRODUKTUA",
                "STOCKA",
                "ERRESERBATUA",
                "ESKURAGARRI"
        );

        exportCsv(destination, headers, rows);
    }


    /**
     * Odoo-ko finantza-idazpenen txostena CSV formatuan esportatzen du.
     * Kontabilitate-idazpen baieztatuak bakarrik hartzen ditu.
     *
     * @param destination CSV fitxategiaren helmuga
     * @throws IOException Esportazioan errorea gertatzen bada
     */
    public void exportFinancialReport(Path destination) throws IOException {

        OdooConnectionService odoo = new OdooConnectionService();

        String json = odoo.getAllPostedJournalItemsForReportJson();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode journalItems = mapper.readTree(json);

        List<List<String>> rows = new ArrayList<>();

        for (JsonNode item : journalItems) {

            JsonNode account = item.path("account_id");

            String accountName = "";

            if (account.isArray() && account.size() > 1) {
                accountName = account.get(1).asText("");
            } else {
                accountName = account.asText("");
            }

            rows.add(List.of(
                    item.path("id").asText(""),
                    item.path("date").asText(""),
                    accountName,
                    item.path("debit").asText("0"),
                    item.path("credit").asText("0")
            ));
        }

        List<String> headers = List.of(
                "IDAZPEN_ID",
                "DATA",
                "KONTUA",
                "ZOR",
                "HARTZEKO"
        );

        exportCsv(destination, headers, rows);
    }
}
