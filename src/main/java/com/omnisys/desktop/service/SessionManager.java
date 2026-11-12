package com.omnisys.desktop.service;

/**
 * Uneko erabiltzailearen saioa kudeatzen du.
 */
public class SessionManager {

    private static String currentUsername;
    private static String currentRole;

    /**
     * Erabiltzailearen saioa hasten du.
     *
     * @param username erabiltzailearen izena
     * @param role erabiltzailearen rola
     */
    public static void login(String username, String role) {

        if (username == null || username.isBlank()
                || (!"ADMIN".equals(role) && !"USER".equals(role))) {

            logout();
            return;
        }

        currentUsername = username;
        currentRole = role;
    }

    /**
     * Uneko erabiltzailearen izena eskuratzen du.
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Uneko erabiltzailearen rola eskuratzen du.
     */
    public static String getCurrentRole() {
        return currentRole;
    }

    /**
     * Uneko erabiltzailea administratzailea den egiaztatzen du.
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(currentRole);
    }

    /**
     * Uneko erabiltzailearen saioa ixten du.
     */
    public static void logout() {

        currentUsername = null;
        currentRole = null;
    }
}