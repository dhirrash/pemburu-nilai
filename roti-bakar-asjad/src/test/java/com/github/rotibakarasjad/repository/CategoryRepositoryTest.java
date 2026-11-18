package com.github.rotibakarasjad.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.rotibakarasjad.entity.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/* Butuh MySQL hidup dengan database roti_bakar_asjad (hasil roti_bakar_asjad.sql). */
class CategoryRepositoryTest {

    private final CategoryRepository repo = new CategoryRepository();

    @BeforeAll
    static void cekDatabase() {
        try (Connection c = DatabaseUtil.getConnection()) {
            assumeTrue(c.isValid(2));
        } catch (Exception e) {
            assumeTrue(false, "Database tidak tersedia: " + e.getMessage());
        }
    }

    @Test
    void findAllMengembalikanKategoriSeed() throws Exception {
        List<Category> all = repo.findAll();
        assertTrue(all.size() >= 4);
    }

    @Test
    void findByIdDanTidakDitemukan() throws Exception {
        assertEquals("Roti Bakar Manis", repo.findById(1).orElseThrow().getName());
        assertTrue(repo.findById(-1).isEmpty());
    }

    @Test
    void insertUpdateDelete() throws Exception {
        Category c = new Category("TEST-" + System.nanoTime());
        int id = repo.insert(c);
        assertTrue(id > 0);
        try {
            c.setName(c.getName() + "-ubah");
            repo.update(c);
            assertEquals(c.getName(), repo.findById(id).orElseThrow().getName());
            assertEquals(0, repo.countProductsByCategory(id));
        } finally {
            repo.delete(id);
        }
        assertTrue(repo.findById(id).isEmpty());
    }

    @Test
    void countProductsByCategoryKategoriSeed() throws Exception {
        assertTrue(repo.countProductsByCategory(1) >= 17);
    }
}
