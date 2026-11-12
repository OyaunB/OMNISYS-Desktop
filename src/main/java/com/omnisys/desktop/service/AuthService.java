package com.omnisys.desktop.service;

import com.omnisys.desktop.dao.UserDAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Erabiltzaileen autentifikazioa eta segurtasuna kudeatzen ditu.
 */
public class AuthService {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final UserDAO userDAO = new UserDAO();

    /**
     * Ausazko salt bat sortzen du.
     */
    private static String generateSalt() {

        byte[] salt = new byte[SALT_LENGTH];

        new SecureRandom().nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Pasahitza PBKDF2 bidez hash bihurtzen du.
     */
    private static String hashPassword(
            String password,
            String salt
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                Base64.getDecoder().decode(salt),
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    /**
     * ADMIN batek erabiltzaile berri bat sortzen du.
     */
    public boolean registerUser(
            String adminUsername,
            String username,
            String password,
            String role
    ) {

        if (!"ADMIN".equals(userDAO.getUserRole(adminUsername))) {
            return false;
        }

        return insertUser(username, password, role);
    }

    /**
     * Hasierako administratzailea sortzen du.
     * Datu-basea hutsik dagoenean bakarrik.
     */
    public boolean createInitialAdmin(
            String username,
            String password
    ) {

        if (!userDAO.getAllUsers().isEmpty()) {
            return false;
        }

        return insertUser(username, password, "ADMIN");
    }

    /**
     * Erabiltzaile berria datu-basean gordetzen du.
     */
    private boolean insertUser(
            String username,
            String password,
            String role
    ) {

        if (username == null || username.isBlank()
                || password == null || password.length() < 5
                || (!"ADMIN".equals(role) && !"USER".equals(role))) {

            return false;
        }

        String salt = generateSalt();

        try {

            String passwordHash = hashPassword(password, salt);

            String sql = """
                    INSERT INTO users (username, password_hash, role)
                    VALUES (?, ?, ?)
                    """;

            try (Connection connection = DatabaseManager.connect()) {

                if (connection == null) {
                    return false;
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, username.trim());
                    statement.setString(2, salt + ":" + passwordHash);
                    statement.setString(3, role);

                    return statement.executeUpdate() > 0;
                }
            }

        } catch (SQLException | NoSuchAlgorithmException
                 | InvalidKeySpecException e) {

            System.err.println(
                    "OMNISYS: Errorea erabiltzailea gordetzean."
            );

            return false;
        }
    }

    /**
     * Erabiltzailearen kredentzialak egiaztatzen ditu.
     */
    public boolean authenticateUser(
            String username,
            String password
    ) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {

            return false;
        }

        String sql = """
                SELECT password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username.trim());

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        return false;
                    }

                    String storedPassword =
                            result.getString("password_hash");

                    if (storedPassword == null) {
                        return false;
                    }

                    String[] parts = storedPassword.split(":", 2);

                    if (parts.length != 2) {
                        return false;
                    }

                    String enteredHash = hashPassword(
                            password,
                            parts[0]
                    );

                    return MessageDigest.isEqual(
                            Base64.getDecoder().decode(parts[1]),
                            Base64.getDecoder().decode(enteredHash)
                    );
                }
            }

        } catch (SQLException | NoSuchAlgorithmException
                 | InvalidKeySpecException | IllegalArgumentException e) {

            System.err.println(
                    "OMNISYS: Errorea autentifikazioa egiaztatzean."
            );

            return false;
        }
    }

    /**
     * Erabiltzaile baten rola eskuratzen du.
     */
    public String getUserRole(String username) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return userDAO.getUserRole(username);
    }

    /**
     * Erabiltzaile bat existitzen den egiaztatzen du.
     */
    public boolean userExists(String username) {

        if (username == null || username.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username.trim());

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "OMNISYS: Errorea erabiltzailea egiaztatzean."
            );

            return false;
        }
    }

    /**
     * ADMIN batek erabiltzaile bat segurtasunez ezabatzen du.
     */
    public boolean deleteUser(
            String adminUsername,
            String targetUsername
    ) {

        if (!"ADMIN".equals(userDAO.getUserRole(adminUsername))) {
            return false;
        }

        // NORBERAREN KONTUA EZABATZEA DEBEKATU
        if (adminUsername.equals(targetUsername)) {
            return false;
        }

        String targetRole = userDAO.getUserRole(targetUsername);

        if (targetRole == null) {
            return false;
        }

        // AZKEN ADMINISTRATZAILEA BABESTU
        if ("ADMIN".equals(targetRole)
                && userDAO.countAdmins() <= 1) {

            return false;
        }

        return userDAO.deleteUser(targetUsername);
    }

    /**
     * ADMIN batek beste erabiltzaile baten rola aldatzen du.
     */
    public boolean changeUserRole(
            String adminUsername,
            String targetUsername,
            String newRole
    ) {

        // ADMIN BAIMENA EGIAZTATU
        if (!"ADMIN".equals(userDAO.getUserRole(adminUsername))) {
            return false;
        }

        // ROLA BALIOZKOA DELA EGIAZTATU
        if (!"ADMIN".equals(newRole) && !"USER".equals(newRole)) {
            return false;
        }

        // ERABILTZAILEA EXISTITZEN DELA EGIAZTATU
        String currentRole = userDAO.getUserRole(targetUsername);

        if (currentRole == null) {
            return false;
        }

        // AZKEN ADMINISTRATZAILEA BABESTU
        if ("ADMIN".equals(currentRole)
                && "USER".equals(newRole)
                && userDAO.countAdmins() <= 1) {

            return false;
        }

        return userDAO.updateUserRole(targetUsername, newRole);
    }
}