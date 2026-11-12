
package com.omnisys.desktop.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisys.desktop.model.Customer;
import com.omnisys.desktop.service.OdooConnectionService;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de recuperar clientes desde Odoo 19.
 */
public class CustomerDAO {

    private final OdooConnectionService odooConnectionService;
    private final ObjectMapper objectMapper;

    public CustomerDAO() {
        this.odooConnectionService = new OdooConnectionService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene los clientes activos registrados en Odoo.
     *
     * @return lista de clientes
     */
    public List<Customer> getAllCustomers() {

        List<Customer> customers = new ArrayList<>();

        try {

            String json = odooConnectionService.getCustomersJson();

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {

                int id = node.path("id").asInt();

                String name = node.path("name").asText("");

                Customer customer = new Customer(
                        id,
                        name
                );

                customers.add(customer);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "OMNISYS: Errorea bezeroak kargatzean.",
                    e
            );
        }

        return customers;
    }

    /**
     * Cuenta los clientes activos de Odoo.
     *
     * @return número total de clientes
     */
    public int getTotalCustomers() {

        return getAllCustomers().size();
    }
}
