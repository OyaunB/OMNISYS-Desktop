
package com.omnisys.desktop.model;

/**
 * Odoo 19-ko bezero bat irudikatzen du.
 *
 * Bezeroaren identifikatzailea eta izena gordetzen ditu.
 */
public class Customer {

    private final int id;
    private final String name;

    public Customer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
