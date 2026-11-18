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

    /** Pekerjaan yang dijalankan di dalam satu transaksi database. */
    @FunctionalInterface
    public interface TxWork<T> {
        T run(Connection conn) throws SQLException;
    }

    /**
     * Menjalankan work dalam satu transaksi.
     * Sukses -> commit. Exception apa pun -> rollback lalu exception dilempar ulang.
     */
    public static <T> T inTransaction(TxWork<T> work) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn);
                throw e;
            }
        }
    }

    private static void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // koneksi akan ditutup oleh try-with-resources
        }
    }
}
