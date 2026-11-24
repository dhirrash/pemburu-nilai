package com.github.rotibakarasjad.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.rotibakarasjad.entity.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductRepositoryTest {

    private final ProductRepository repo = new ProductRepository();

    @BeforeAll
    static void cekDatabase() {
        try (Connection c = DatabaseUtil.getConnection()) {
            assumeTrue(c.isValid(2));
        } catch (Exception e) {
            assumeTrue(false, "Database tidak tersedia: " + e.getMessage());
        }
    }

    @Test
    void findActiveHanyaProdukAktif() throws Exception {
        assertFalse(repo.findActive().isEmpty());
        assertTrue(repo.findActive().stream().allMatch(Product::isActive));
    }

    @Test
    void findByIdMembawaNamaKategori() throws Exception {
        Product p = repo.findById(1).orElseThrow();
        assertEquals("Roti Bakar Coklat Keju", p.getName());
        assertEquals("Roti Bakar", p.getCategoryName());
    }

    @Test
    void insertUpdateDanUpdateStock() throws Exception {
        Product p = new Product(1, "TEST-" + System.nanoTime(), null, new BigDecimal("1000"), 5, true);
        int id = repo.insert(p);
        try (Connection conn = DatabaseUtil.getConnection()) {
            assertTrue(repo.updateStock(conn, id, 3));
            assertEquals(8, repo.findById(id).orElseThrow().getStock());
            assertFalse(repo.updateStock(conn, id, -9), "stok tidak boleh negatif");
            assertEquals(8, repo.findById(id).orElseThrow().getStock());
        }
        p.setPrice(new BigDecimal("2000"));
        p.setActive(false);
        repo.update(p);
        Product dbP = repo.findById(id).orElseThrow();
        assertEquals(0, new BigDecimal("2000").compareTo(dbP.getPrice()));
        assertFalse(dbP.isActive());

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        }
    }
}
