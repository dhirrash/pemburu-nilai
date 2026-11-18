package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.OrderItem;
import com.github.rotibakarasjad.model.OrderItemRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* Semua method menerima Connection agar bisa ikut transaksi milik service. */
public class OrderItemRepository {

    private static final String SELECT =
            "SELECT id, order_id, product_id, product_name, unit_price, quantity, subtotal FROM order_items ";

    /* Urut product_id supaya urutan penguncian produk selalu sama (cegah deadlock). */
    public List<OrderItem> findByOrder(Connection conn, int orderId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE order_id = ? ORDER BY product_id")) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderItem> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    public List<OrderItemRow> findRowsByOrder(Connection conn, int orderId) throws SQLException {
        List<OrderItemRow> rows = new ArrayList<>();
        for (OrderItem i : findByOrder(conn, orderId)) {
            rows.add(new OrderItemRow(i.getId(), i.getProductId(), i.getProductName(),
                    i.getUnitPrice(), i.getQuantity()));
        }
        return rows;
    }

    public Optional<OrderItem> findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<OrderItem> findByOrderAndProduct(Connection conn, int orderId, int productId)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE order_id = ? AND product_id = ?")) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /* Item baru: nama dan harga disalin dari produk saat ini. */
    public void insert(Connection conn, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setBigDecimal(4, item.getUnitPrice());
            stmt.setInt(5, item.getQuantity());
            stmt.setBigDecimal(6, item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
            stmt.executeUpdate();
        }
    }

    /* Ubah kuantitas. Harga satuan lama (yang sudah dikunci) dipertahankan. */
    public void updateQuantity(Connection conn, int orderItemId, int quantity) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ?, subtotal = unit_price * ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, orderItemId);
            stmt.executeUpdate();
        }
    }

    public void delete(Connection conn, int orderItemId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM order_items WHERE id = ?")) {
            stmt.setInt(1, orderItemId);
            stmt.executeUpdate();
        }
    }

    private OrderItem map(ResultSet rs) throws SQLException {
        OrderItem i = new OrderItem();
        i.setId(rs.getInt("id"));
        i.setOrderId(rs.getInt("order_id"));
        i.setProductId(rs.getInt("product_id"));
        i.setProductName(rs.getString("product_name"));
        i.setUnitPrice(rs.getBigDecimal("unit_price"));
        i.setQuantity(rs.getInt("quantity"));
        i.setSubtotal(rs.getBigDecimal("subtotal"));
        return i;
    }
}
