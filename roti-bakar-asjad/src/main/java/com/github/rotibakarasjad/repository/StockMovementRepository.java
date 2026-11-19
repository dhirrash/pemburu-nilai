package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.StockMovement;
import com.github.rotibakarasjad.entity.enums.StockReason;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StockMovementRepository {

    /* Dipakai di dalam transaksi (pembayaran, restok, penyesuaian). */
    public int insert(Connection conn, StockMovement m) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, order_id, change_qty, reason, note) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, m.getProductId());
            if (m.getOrderId() == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, m.getOrderId());
            }
            stmt.setInt(3, m.getChangeQty());
            stmt.setString(4, m.getReason().name());
            stmt.setString(5, m.getNote());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setId(keys.getInt(1));
                    return m.getId();
                }
                throw new SQLException("Id stock movement tidak dikembalikan database.");
            }
        }
    }

    public List<StockMovement> findByProduct(int productId) throws SQLException {
        String sql = "SELECT id, product_id, order_id, change_qty, reason, note, created_at "
                   + "FROM stock_movements WHERE product_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<StockMovement> result = new ArrayList<>();
                while (rs.next()) {
                    StockMovement m = new StockMovement();
                    m.setId(rs.getInt("id"));
                    m.setProductId(rs.getInt("product_id"));
                    int orderId = rs.getInt("order_id");
                    m.setOrderId(rs.wasNull() ? null : orderId);
                    m.setChangeQty(rs.getInt("change_qty"));
                    m.setReason(StockReason.valueOf(rs.getString("reason")));
                    m.setNote(rs.getString("note"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
                    result.add(m);
                }
                return result;
            }
        }
    }
}
