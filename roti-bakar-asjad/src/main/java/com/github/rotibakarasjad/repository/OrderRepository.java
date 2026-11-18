package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.Order;
import com.github.rotibakarasjad.entity.enums.OrderStatus;
import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import com.github.rotibakarasjad.model.OrderHistoryRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* Semua method menerima Connection agar bisa ikut transaksi milik service. */
public class OrderRepository {

    private static final String SELECT =
            "SELECT id, order_code, user_id, total_amount, paid_amount, change_amount, "
            + "payment_method, status, created_at, paid_at FROM orders ";

    public Optional<Order> findById(Connection conn, int id) throws SQLException {
        return findOne(conn, SELECT + "WHERE id = ?", id);
    }

    /* Mengunci baris pesanan agar tidak diubah dua proses sekaligus. */
    public Optional<Order> findByIdForUpdate(Connection conn, int id) throws SQLException {
        return findOne(conn, SELECT + "WHERE id = ? FOR UPDATE", id);
    }

    public Optional<Order> findOpenOrderByUser(Connection conn, int userId) throws SQLException {
        return findOne(conn, SELECT + "WHERE user_id = ? AND status = 'OPEN' ORDER BY id DESC LIMIT 1", userId);
    }

    /* Mengembalikan id baru. order.orderCode dan order.userId harus sudah terisi. */
    public int insert(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_code, user_id, status) VALUES (?, ?, 'OPEN')";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getOrderCode());
            stmt.setInt(2, order.getUserId());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                    return order.getId();
                }
                throw new SQLException("Id pesanan tidak dikembalikan database.");
            }
        }
    }

    /* Nomor urut terakhir hari ini (0 jika belum ada). prefix contoh: "RBA-20261118-". */
    public int findLastSequence(Connection conn, String prefix) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(order_code, ?) AS UNSIGNED)), 0) "
                   + "FROM orders WHERE order_code LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prefix.length() + 1);
            stmt.setString(2, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* Hitung ulang total_amount dari item. Mengembalikan total baru. */
    public BigDecimal recalculateTotal(Connection conn, int orderId) throws SQLException {
        String upd = "UPDATE orders SET total_amount = "
                   + "(SELECT COALESCE(SUM(subtotal), 0) FROM order_items WHERE order_id = ?) WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(upd)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT total_amount FROM orders WHERE id = ?")) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public void updateStatus(Connection conn, int orderId, OrderStatus status) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }

    /* Menandai PAID. Dipanggil hanya dari OrderService.payOrder di dalam transaksi. */
    public void markPaid(Connection conn, int orderId, BigDecimal total, BigDecimal paid,
                         BigDecimal change, PaymentMethod method) throws SQLException {
        String sql = "UPDATE orders SET status = 'PAID', total_amount = ?, paid_amount = ?, "
                   + "change_amount = ?, payment_method = ?, paid_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, total);
            stmt.setBigDecimal(2, paid);
            stmt.setBigDecimal(3, change);
            stmt.setString(4, method.name());
            stmt.setInt(5, orderId);
            stmt.executeUpdate();
        }
    }

    /* Riwayat PAID dengan paid_at dalam [from, toExclusive). Terbaru di atas. */
    public List<OrderHistoryRow> findPaidBetween(Connection conn, LocalDateTime from,
                                                 LocalDateTime toExclusive) throws SQLException {
        String sql = "SELECT o.id, o.order_code, u.full_name, o.total_amount, o.paid_amount, "
                   + "o.change_amount, o.payment_method, o.paid_at "
                   + "FROM orders o JOIN users u ON o.user_id = u.id "
                   + "WHERE o.status = 'PAID' AND o.paid_at >= ? AND o.paid_at < ? "
                   + "ORDER BY o.paid_at DESC, o.id DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(from));
            stmt.setTimestamp(2, Timestamp.valueOf(toExclusive));
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderHistoryRow> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new OrderHistoryRow(
                            rs.getInt("id"),
                            rs.getString("order_code"),
                            rs.getString("full_name"),
                            rs.getBigDecimal("total_amount"),
                            rs.getBigDecimal("paid_amount"),
                            rs.getBigDecimal("change_amount"),
                            PaymentMethod.valueOf(rs.getString("payment_method")),
                            rs.getTimestamp("paid_at").toLocalDateTime()));
                }
                return rows;
            }
        }
    }

    private Optional<Order> findOne(Connection conn, String sql, int param) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setUserId(rs.getInt("user_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPaidAmount(rs.getBigDecimal("paid_amount"));
        o.setChangeAmount(rs.getBigDecimal("change_amount"));
        String pm = rs.getString("payment_method");
        o.setPaymentMethod(pm == null ? null : PaymentMethod.valueOf(pm));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        o.setCreatedAt(created == null ? null : created.toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        o.setPaidAt(paid == null ? null : paid.toLocalDateTime());
        return o;
    }
}
