package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.Category;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.repository.CategoryRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Aturan bisnis kategori produk.
 */
public class CategoryService {

    private static final int MAX_NAME = 100;

    /** Kategori beserta jumlah produk yang memakainya (untuk tabel admin). */
    public record CategoryUsage(int id, String name, int productCount) {
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    private final CategoryRepository repo = new CategoryRepository();

    public List<Category> findAll() {
        return call(repo::findAll);
    }

    public List<CategoryUsage> getAllWithUsage() {
        return call(() -> {
            List<CategoryUsage> result = new ArrayList<>();
            for (Category c : repo.findAll()) {
                result.add(new CategoryUsage(c.getId(), c.getName(),
                        repo.countProductsByCategory(c.getId())));
            }
            return result;
        });
    }

    public void addCategory(String name) {
        String nama = validName(name);
        ensureUniqueName(nama, -1);
        call(() -> repo.insert(new Category(nama)));
    }

    public void renameCategory(int id, String name) {
        String nama = validName(name);
        ensureUniqueName(nama, id);
        call(() -> {
            Category category = repo.findById(id)
                    .orElseThrow(() -> new BusinessException("Kategori tidak ditemukan."));
            category.setName(nama);
            repo.update(category);
            return null;
        });
    }

    /** Kategori yang masih dipakai produk tidak boleh dihapus. */
    public void deleteCategory(int id) {
        int used = call(() -> repo.countProductsByCategory(id));
        if (used > 0) {
            throw new BusinessException("Kategori tidak bisa dihapus karena masih dipakai "
                    + used + " produk.");
        }
        call(() -> {
            repo.delete(id);
            return null;
        });
    }

    // ---------------------------------------------------------------- helper

    private void ensureUniqueName(String nama, int exceptId) {
        boolean sudahAda = call(() -> repo.findAll().stream()
                .anyMatch(c -> c.getId() != exceptId && c.getName().equalsIgnoreCase(nama)));
        if (sudahAda) {
            throw new BusinessException("Nama kategori \"" + nama + "\" sudah ada.");
        }
    }

    private static String validName(String name) {
        String nama = name == null ? "" : name.trim();
        if (nama.isEmpty()) {
            throw new BusinessException("Nama kategori tidak boleh kosong.");
        }
        if (nama.length() > MAX_NAME) {
            throw new BusinessException("Nama kategori maksimal " + MAX_NAME + " karakter.");
        }
        return nama;
    }

    private static <T> T call(SqlSupplier<T> work) {
        try {
            return work.get();
        } catch (SQLException e) {
            throw new BusinessException("Terjadi kesalahan database: " + e.getMessage(), e);
        }
    }
}
