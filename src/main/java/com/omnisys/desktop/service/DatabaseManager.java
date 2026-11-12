package com.omnisys.desktop.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite datu-basearen konexioa eta hasieraketa kudeatzen ditu.
 */
public class DatabaseManager {

    private static final String DATABASE_URL =
            "jdbc:sqlite:omnisys.db";

    /**
     * SQLite datu-basera konektatzen da.
     *
     * @return datu-basearen konexioa
     */
    public static Connection connect() {

        try {
            return DriverManager.getConnection(DATABASE_URL);

        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Erabiltzaileen taula sortzen du eta beharrezko migrazioak egiten ditu.
     */
    public static void createUsersTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Connection connection = connect()) {

            if (connection == null) {
                throw new SQLException(
                        "Ezin izan da datu-basera konektatu."
                );
            }

            try (Statement statement = connection.createStatement()) {

                statement.execute(sql);

                // LEHENAGOKO DATU-BASEEN MIGRAZIOA
                if (!columnExists(connection, "role")) {

                    statement.execute("""
                            ALTER TABLE users
                            ADD COLUMN role TEXT NOT NULL DEFAULT 'USER'
                            """);
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "OMNISYS: Ezin izan da erabiltzaileen datu-basea prestatu.",
                    e
            );
        }
    }

    /**
     * Taula batean zutabe bat existitzen den egiaztatzen du.
     *
     * @param connection datu-basearen konexioa
     * @param columnName egiaztatu beharreko zutabearen izena
     * @return true zutabea existitzen bada
     * @throws SQLException kontsultan errorea gertatzen bada
     */
    private static boolean columnExists(
            Connection connection,
            String columnName
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(users)"
             )) {

            while (resultSet.next()) {

                if (columnName.equalsIgnoreCase(
                        resultSet.getString("name")
                )) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * OMNISYS datu-basea hasieratzen du.
     */
    public static void initializeDatabase() {

        createUsersTable();
    }
}