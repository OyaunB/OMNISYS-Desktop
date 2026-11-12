package com.omnisys.desktop.dao;

import com.omnisys.desktop.model.User;
import com.omnisys.desktop.service.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Erabiltzaileen datu-baseko eragiketak kudeatzen ditu.
 */
public class UserDAO {

    /**
     * Erabiltzaile guztiak eskuratzen ditu.
     *
     * @return erabiltzaileen zerrenda
     */
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, role, created_at
                FROM users
                ORDER BY id ASC
                """;

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return users;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    User user = new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("role"),
                            result.getString("created_at")
                    );

                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "OMNISYS: Errorea erabiltzaileak eskuratzean."
            );
        }

        return users;
    }

    /**
     * Erabiltzaile baten rola eskuratzen du.
     *
     * @param username erabiltzailearen izena
     * @return erabiltzailearen rola edo null
     */
    public String getUserRole(String username) {

        String sql = "SELECT role FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return null;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return result.getString("role");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "OMNISYS: Errorea erabiltzailearen rola eskuratzean."
            );
        }

        return null;
    }

    /**
     * Administratzaile kopurua kontatzen du.
     *
     * @return administratzaile kopurua
     */
    public int countAdmins() {

        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return 0;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "OMNISYS: Errorea administratzaileak kontatzean."
            );
        }

        return 0;
    }

    /**
     * Erabiltzaile bat ezabatzen du.
     *
     * @param username ezabatu beharreko erabiltzailearen izena
     * @return true ezabaketa arrakastatsua bada
     */
    public boolean deleteUser(String username) {

        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                    "OMNISYS: Errorea erabiltzailea ezabatzean."
            );
        }

        return false;
    }

    /**
     * Erabiltzaile baten rola eguneratzen du.
     *
     * @param username erabiltzailearen izena
     * @param role rol berria
     * @return true eguneraketa arrakastatsua bada
     */
    public boolean updateUserRole(String username, String role) {

        String sql = """
                UPDATE users
                SET role = ?
                WHERE username = ?
                """;

        try (Connection connection = DatabaseManager.connect()) {

            if (connection == null) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, role);
                statement.setString(2, username);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                    "OMNISYS: Errorea erabiltzailearen rola eguneratzean."
            );
        }

        return false;
    }
}