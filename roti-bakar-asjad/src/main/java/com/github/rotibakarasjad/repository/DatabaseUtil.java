package com.github.rotibakarasjad.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseUtil {

    private static final String URL = System.getenv().getOrDefault("RBA_DB_URL",
            "jdbc:mysql://localhost:3306/roti_bakar_asjad"
            + "?serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true&useSSL=false");
    private static final String USERNAME = System.getenv().getOrDefault("RBA_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("RBA_DB_PASSWORD", "");

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}