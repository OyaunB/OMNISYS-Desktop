package com.omnisys.desktop.model;

/**
 * Representa un usuario registrado en OMNISYS DESKTOP.
 *
 * Contiene su identificador, nombre de usuario,
 * rol y fecha de creación.
 */
public class User {

    private final int id;
    private final String username;
    private final String role;
    private final String createdAt;

    /**
     * Crea un nuevo usuario.
     *
     * @param id identificador del usuario
     * @param username nombre de usuario
     * @param role rol del usuario (ADMIN o USER)
     * @param createdAt fecha de creación de la cuenta
     */
    public User(int id, String username, String role, String createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}