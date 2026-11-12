package com.omnisys.desktop.model;

import java.time.YearMonth;

public class MonthlyChartData {

    private final YearMonth yearMonth;
    private final double sales;
    private final double purchases;

    public MonthlyChartData(YearMonth yearMonth, double sales, double purchases) {
        this.yearMonth = yearMonth;
        this.sales = sales;
        this.purchases = purchases;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public double getSales() {
        return sales;
    }

    public double getPurchases() {
        return purchases;
    }

    public String getMonthName() {
        return switch (yearMonth.getMonthValue()) {
            case 1 -> "URTARRILA";
            case 2 -> "OTSAILA";
            case 3 -> "MARTXOA";
            case 4 -> "APIRILA";
            case 5 -> "MAIATZA";
            case 6 -> "EKAINA";
            case 7 -> "UZTAILA";
            case 8 -> "ABUZTUA";
            case 9 -> "IRAILA";
            case 10 -> "URRIA";
            case 11 -> "AZAROA";
            case 12 -> "ABENDUA";
            default -> "";
        };
    }
}