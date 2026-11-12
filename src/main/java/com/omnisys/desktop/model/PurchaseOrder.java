
package com.omnisys.desktop.model;

/**
 * Odoo 19-ko erosketa-eskaera bat irudikatzen du.
 *
 * Eskaeraren identifikatzailea, erreferentzia,
 * hornitzailea, data, guztizko zenbatekoa eta egoera gordetzen ditu.
 */
public class PurchaseOrder {

    private final int id;
    private final String name;
    private final String partnerName;
    private final String dateOrder;
    private final double amountTotal;
    private final String state;

    public PurchaseOrder(
            int id,
            String name,
            String partnerName,
            String dateOrder,
            double amountTotal,
            String state
    ) {
        this.id = id;
        this.name = name;
        this.partnerName = partnerName;
        this.dateOrder = dateOrder;
        this.amountTotal = amountTotal;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getDateOrder() {
        return dateOrder;
    }

    public double getAmountTotal() {
        return amountTotal;
    }

    public String getState() {
        return state;
    }
}
