
package com.omnisys.desktop.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Odoo 19 API konexioaren kudeaketa.
 */
public class OdooConnectionService {

    private static final String ODOO_URL =
            "http://192.168.80.118:8069";

    private static final String ODOO_DATABASE =
            "odoo-erronka";

    private static final String API_KEY_VARIABLE =
            "OMNISYS_ODOO_API_KEY";

    private final HttpClient httpClient;

    public OdooConnectionService() {

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Odoo API bidez produktuak eskuratzen ditu.
     *
     * @return Odoo-ko produktuen JSON erantzuna
     */
    public String getProductsJson() {

        String requestBody = """
                {
                    "domain": [["active", "=", true]],
                    "fields": [
                        "id",
                        "name",
                        "default_code",
                        "list_price",
                        "image_512"
                    ],
                    "limit": 50,
                    "order": "name asc"
                }
                """;

        return searchReadJson(
                "product.template",
                requestBody
        );
    }

    /**
     * Hilabete honetako salmenta-eskaera baieztatuak eskuratzen ditu.
     *
     * Aurrekontuak baztertzen dira.
     *
     * @return Odoo-ko salmenta-eskaeren JSON erantzuna
     */
    public String getSalesThisMonthJson() {

        LocalDate today = LocalDate.now();

        LocalDate firstDay = today.withDayOfMonth(1);

        LocalDate firstDayNextMonth = firstDay.plusMonths(1);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDay.format(formatter)
                + " 00:00:00";

        String endDate = firstDayNextMonth.format(formatter)
                + " 00:00:00";

        String requestBody = """
                {
                    "domain": [
                        ["state", "in", ["sale", "done"]],
                        ["date_order", ">=", "%s"],
                        ["date_order", "<", "%s"]
                    ],
                    "fields": [
                        "id",
                        "name",
                        "partner_id",
                        "date_order",
                        "amount_total",
                        "state"
                    ],
                    "limit": 1000,
                    "order": "date_order desc"
                }
                """.formatted(startDate, endDate);

        return searchReadJson(
                "sale.order",
                requestBody
        );
    }


    /**
     * Odoo-ko kontabilitate-idazpen argitaratu guztiak esportatzeko eskuratzen ditu.
     *
     * @return Odoo-ko kontabilitate-apuntuen JSON erantzuna
     */
    public String getAllPostedJournalItemsForReportJson() {

        String requestBody = """
        {
            "domain": [
                ["parent_state", "=", "posted"]
            ],
            "fields": [
                "id",
                "date",
                "account_id",
                "debit",
                "credit"
            ],
            "limit": 10000,
            "order": "date desc"
        }
        """;

        return searchReadJson(
                "account.move.line",
                requestBody
        );
    }



    /**
     * Odoo-ko erosketa-eskaera baieztatu guztiak esportatzeko eskuratzen ditu.
     *
     * @return Odoo-ko erosketa-eskaeren JSON erantzuna
     */
    public String getAllPurchasesForReportJson() {

        String requestBody = """
        {
            "domain": [
                ["state", "in", ["purchase", "done"]]
            ],
            "fields": [
                "id",
                "name",
                "partner_id",
                "date_order",
                "amount_total",
                "state"
            ],
            "limit": 10000,
            "order": "date_order desc"
        }
        """;

        return searchReadJson(
                "purchase.order",
                requestBody
        );
    }



    /**
     * Odoo-ko salmenta-eskaera baieztatu guztiak esportatzeko eskuratzen ditu.
     *
     * @return Odoo-ko salmenten JSON erantzuna
     */
    public String getAllSalesForReportJson() {

        String requestBody = """
        {
            "domain": [
                ["state", "in", ["sale", "done"]]
            ],
            "fields": [
                "id",
                "name",
                "partner_id",
                "date_order",
                "amount_total",
                "state"
            ],
            "limit": 10000,
            "order": "date_order desc"
        }
        """;

        return searchReadJson(
                "sale.order",
                requestBody
        );
    }


    /**
     * Hautatutako hilabeteko eta urteko salmentak eskuratzen ditu.
     */
    public String getSalesByMonthYearJson(int month, int year) {

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDay.format(formatter) + " 00:00:00";
        String endDate = firstDayNextMonth.format(formatter) + " 00:00:00";

        String requestBody = """
            {
                "domain": [
                    ["state", "in", ["sale", "done"]],
                    ["date_order", ">=", "%s"],
                    ["date_order", "<", "%s"]
                ],
                "fields": [
                    "id",
                    "name",
                    "partner_id",
                    "date_order",
                    "amount_total",
                    "state"
                ],
                "limit": 1000,
                "order": "date_order desc"
            }
            """.formatted(startDate, endDate);

        return searchReadJson(
                "sale.order",
                requestBody
        );
    }

    /**
     * Hilabete honetako erosketa-eskaera baieztatuak eskuratzen ditu.
     *
     * @return Odoo-ko erosketa-eskaeren JSON erantzuna
     */
    public String getPurchasesThisMonthJson() {

        LocalDate today = LocalDate.now();

        LocalDate firstDay = today.withDayOfMonth(1);

        LocalDate firstDayNextMonth = firstDay.plusMonths(1);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDay.format(formatter)
                + " 00:00:00";

        String endDate = firstDayNextMonth.format(formatter)
                + " 00:00:00";

        String requestBody = """
            {
                "domain": [
                    ["state", "in", ["purchase", "done"]],
                    ["date_order", ">=", "%s"],
                    ["date_order", "<", "%s"]
                ],
                "fields": [
                    "id",
                    "name",
                    "partner_id",
                    "date_order",
                    "amount_total",
                    "state"
                ],
                "limit": 1000,
                "order": "date_order desc"
            }
            """.formatted(startDate, endDate);

        return searchReadJson(
                "purchase.order",
                requestBody
        );
    }

    /**
     * Aukeratutako hilabete eta urteko erosketa-eskaera baieztatuak eskuratzen ditu.
     *
     * @param month Aukeratutako hilabetea
     * @param year Aukeratutako urtea
     * @return Odoo-ko erosketa-eskaeren JSON erantzuna
     */
    public String getPurchasesByMonthYearJson(int month, int year) {

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDay.format(formatter) + " 00:00:00";
        String endDate = firstDayNextMonth.format(formatter) + " 00:00:00";

        String requestBody = """
        {
            "domain": [
                ["state", "in", ["purchase", "done"]],
                ["date_order", ">=", "%s"],
                ["date_order", "<", "%s"]
            ],
            "fields": [
                "id",
                "name",
                "partner_id",
                "date_order",
                "amount_total",
                "state"
            ],
            "limit": 1000,
            "order": "date_order desc"
        }
        """.formatted(startDate, endDate);

        return searchReadJson(
                "purchase.order",
                requestBody
        );
    }

    /**
     * Odoo-ko bezero aktiboak eskuratzen ditu.
     *
     * @return Odoo-ko bezeroen JSON erantzuna
     */
    public String getCustomersJson() {

        String requestBody = """
            {
                "domain": [
                    ["active", "=", true],
                    ["customer_rank", ">", 0]
                ],
                "fields": [
                    "id",
                    "name"
                ],
                "limit": 10000,
                "order": "name asc"
            }
            """;

        return searchReadJson(
                "res.partner",
                requestBody
        );
    }


    /**
     * Odoo-ko produktu aktiboak eskuratzen ditu.
     *
     * @return Odoo-ko produktuen JSON erantzuna
     */
    public String getActiveProductsJson() {

        String requestBody = """
            {
                "domain": [
                    ["active", "=", true]
                ],
                "fields": [
                    "id",
                    "name"
                ],
                "limit": 10000,
                "order": "name asc"
            }
            """;

        return searchReadJson(
                "product.template",
                requestBody
        );
    }

    /**
     * Biltegian eskuragarri dauden stock-kantitateak eskuratzen ditu.
     *
     * Barne-kokapenetako kantitateak soilik hartzen ditu.
     *
     * @return Odoo-ko stock kuantitateen JSON erantzuna
     */
    public String getAvailableStockJson() {

        String requestBody = """
            {
                "domain": [
                    ["location_id.usage", "=", "internal"],
                    ["product_id.active", "=", true]
                ],
                "fields": [
                    "product_id",
                    "quantity",
                    "reserved_quantity"
                ],
                "limit": 10000
            }
            """;

        return searchReadJson(
                "stock.quant",
                requestBody
        );
    }

    /**
     * Hilabete honetako kontabilitate-idazpen argitaratuak eskuratzen ditu.
     *
     * @return Odoo-ko kontabilitate-apuntuen JSON erantzuna
     */
    public String getPostedJournalItemsThisMonthJson() {

        LocalDate today = LocalDate.now();

        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDay.format(formatter);
        String endDate = firstDayNextMonth.format(formatter);

        String requestBody = """
            {
                "domain": [
                    ["parent_state", "=", "posted"],
                    ["date", ">=", "%s"],
                    ["date", "<", "%s"]
                ],
                "fields": [
                    "id",
                    "date",
                    "account_id",
                    "debit",
                    "credit"
                ],
                "limit": 10000,
                "order": "date asc"
            }
            """.formatted(startDate, endDate);

        return searchReadJson(
                "account.move.line",
                requestBody
        );
    }

    /**
     * Kontabilitate-kontuak eta haien motak eskuratzen ditu.
     *
     * @return Odoo-ko kontuen JSON erantzuna
     */
    public String getAccountsJson() {

        String requestBody = """
            {
                "domain": [],
                "fields": [
                    "id",
                    "code",
                    "name",
                    "account_type"
                ],
                "limit": 10000,
                "order": "code asc"
            }
            """;

        return searchReadJson(
                "account.account",
                requestBody
        );
    }

    /**
     * Odoo-ko modelo baten search_read kontsulta egiten du.
     *
     * @param model Odoo-ko modeloaren izena
     * @param requestBody JSON eskaeraren edukia
     * @return Odoo-ren JSON erantzuna
     */
    private String searchReadJson(
            String model,
            String requestBody
    ) {

        String apiKey = System.getenv(API_KEY_VARIABLE);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OMNISYS: Odoo API Key falta da."
            );
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        ODOO_URL + "/json/2/" + model + "/search_read"
                ))
                .header("Authorization", "Bearer " + apiKey)
                .header("X-Odoo-Database", ODOO_DATABASE)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        try {

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "OMNISYS: Odoo HTTP errorea: "
                                + response.statusCode()
                );
            }

            return response.body();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "OMNISYS: Odoo kontsulta eten egin da.", e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea Odoo-ko datuak eskuratzean.", e
            );
        }
    }
}
