package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    private static final String BASE_SELECT =
            "SELECT p.id, p.category_id, c.name AS category_name, p.name, p.description, "
            + "       p.price, p.stock, p.is_active "
            + "FROM products AS p "
            + "JOIN categories AS c ON p.category_id = c.id ";

    /* Semua produk, termasuk yang nonaktif. Untuk halaman admin. */
    public List<Product> findAll() throws SQLException {
        return queryList(BASE_SELECT + "ORDER BY p.id");
    }

    /* Hanya produk aktif. Untuk katalog kasir. */
    public List<Product> findActive() throws SQLException {
        return queryList(BASE_SELECT + "WHERE p.is_active = 1 ORDER BY p.id");
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /*
     * Menyimpan produk baru dan mengembalikan id yang dibuat database.
     * Validasi input dilakukan di ProductService (minggu 2).
     */
    public int insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, name, description, price, stock, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setBoolean(6, product.isActive());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    product.setId(id);
                    return id;
                }
                throw new SQLException("Id produk tidak dikembalikan database.");
            }
        }
    }

    /* Versi dengan Connection, dipakai di dalam transaksi. */
    public Optional<Product> findById(Connection conn, int id) throws SQLException {
        return findOne(conn, BASE_SELECT + "WHERE p.id = ?", id);
    }

    /* Mengunci baris produk (SELECT ... FOR UPDATE). Hanya untuk dalam transaksi. */
    public Optional<Product> findByIdForUpdate(Connection conn, int id) throws SQLException {
        return findOne(conn, BASE_SELECT + "WHERE p.id = ? FOR UPDATE", id);
    }

    private Optional<Product> findOne(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /* Mengubah data produk. Stok TIDAK diubah di sini (pakai updateStock). */
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, "
                   + "price = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setBoolean(5, product.isActive());
            stmt.setInt(6, product.getId());
            stmt.executeUpdate();
        }
    }

    /*
     * Menambah (delta positif) atau mengurangi (delta negatif) stok secara atomik.
     * Mengembalikan false jika stok akan menjadi negatif (tidak ada yang berubah).
     */
    public boolean updateStock(Connection conn, int productId, int delta) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ? AND stock + ? >= 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, productId);
            stmt.setInt(3, delta);
            return stmt.executeUpdate() == 1;
        }
    }

    private List<Product> queryList(String sql) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Product> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setActive(rs.getBoolean("is_active"));
        return product;
    }
}