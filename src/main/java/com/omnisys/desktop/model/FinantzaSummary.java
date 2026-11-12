
package com.omnisys.desktop.model;

/**
 * Enpresaren hileko finantza-laburpena gordetzen du.
 */
public class FinantzaSummary {

    private final double income;
    private final double expenses;
    private final double profit;

    public FinantzaSummary(double income, double expenses) {
        this.income = income;
        this.expenses = expenses;
        this.profit = income - expenses;
    }

    public double getIncome() {
        return income;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getProfit() {
        return profit;
    }
}
