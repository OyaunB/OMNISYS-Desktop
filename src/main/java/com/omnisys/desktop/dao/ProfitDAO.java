
package com.omnisys.desktop.dao;

import com.omnisys.desktop.model.FinantzaSummary;

/**
 * Hilabete honetako salmenten eta erosketen arteko aldea kalkulatzen du.
 */
public class ProfitDAO {

    private final SaleOrderDAO saleOrderDAO;
    private final PurchaseOrderDAO purchaseOrderDAO;

    public ProfitDAO() {
        this.saleOrderDAO = new SaleOrderDAO();
        this.purchaseOrderDAO = new PurchaseOrderDAO();
    }

    /**
     * Hilabete honetako salmentak, erosketak eta irabaziak kalkulatzen ditu.
     *
     * @return hileko finantza-laburpena
     */
    public FinantzaSummary getMonthlyFinancialSummary() {

        double sales = saleOrderDAO.getMonthlySalesTotal();

        double purchases = purchaseOrderDAO.getMonthlyPurchasesTotal();

        return new FinantzaSummary(sales, purchases);
    }

    public FinantzaSummary getFinancialSummaryByMonthYear(int month, int year) {

        double sales = saleOrderDAO
                .getSalesByMonthYear(month, year)
                .stream()
                .mapToDouble(order -> order.getAmountTotal())
                .sum();

        double purchases = purchaseOrderDAO
                .getPurchasesByMonthYear(month, year)
                .stream()
                .mapToDouble(order -> order.getAmountTotal())
                .sum();

        return new FinantzaSummary(sales, purchases);
    }

    /**
     * Hilabete honetako irabazi komertzial sinplifikatua kalkulatzen du.
     *
     * @return salmentak ken erosketak
     */
    public double getMonthlyProfit() {

        return getMonthlyFinancialSummary().getProfit();
    }
}
