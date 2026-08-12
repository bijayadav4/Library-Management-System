package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place that knows how to open a JDBC connection to PostgreSQL.
 *
 * Edit URL / USER / PASSWORD below to match your local setup, or export
 * them as environment variables (DB_URL, DB_USER, DB_PASSWORD) — env vars
 * win if present, so you don't have to edit source to change environments.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/library_db";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "98029bijay";

    private static final String URL =
        System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
    private static final String USER =
        System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
    private static final String PASSWORD =
        System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);

    /**
     * Opens and returns a new connection. Callers are responsible for
     * closing it (try-with-resources is used everywhere in this project).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
