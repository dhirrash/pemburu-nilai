package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.entity.StockMovement;
import com.github.rotibakarasjad.entity.enums.StockReason;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.repository.DatabaseUtil;
import com.github.rotibakarasjad.repository.ProductRepository;
import com.github.rotibakarasjad.repository.StockMovementRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/*
 * Aturan bisnis produk dan stok (untuk admin dan katalog kasir).
 * Setiap perubahan stok dicatat di stock_movements dalam satu transaksi.
 */
public class ProductService {

    private static final int MAX_NAME = 150;
    private static final int MAX_DESCRIPTION = 500;

    private final ProductRepository productRepo = new ProductRepository();
    private final StockMovementRepository movementRepo = new StockMovementRepository();

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    /* Produk aktif untuk katalog kasir. */
    public List<Product> getActiveProducts() {
        return call(productRepo::findActive);
    }

    /* Semua produk termasuk yang nonaktif, untuk halaman admin. */
    public List<Product> getAllProducts() {
        return call(productRepo::findAll);
    }

    /* Membuat produk baru. Jika stok awal > 0, tercatat sebagai RESTOCK. */
    public void createProduct(String name, String description, int categoryId,
                              BigDecimal price, int initialStock) {
        String nama = validName(name);
        validPrice(price);
        if (categoryId <= 0) {
            throw new BusinessException("Kategori wajib dipilih.");
        }
        if (initialStock < 0) {
            throw new BusinessException("Stok awal tidak boleh negatif.");
        }

        Product product = new Product(categoryId, nama, blankToNull(description, MAX_DESCRIPTION),
                price, initialStock, true);
        write(conn -> {
            productRepo.insert(conn, product);
            if (initialStock > 0) {
                movementRepo.insert(conn, movement(product.getId(), initialStock,
                        StockReason.RESTOCK, "Stok awal"));
            }
            return null;
        });
    }

    /* Mengubah nama, kategori, dan harga. Stok tidak berubah di sini. */
    public void updateProduct(int id, String name, int categoryId, BigDecimal price) {
        String nama = validName(name);
        validPrice(price);
        if (categoryId <= 0) {
            throw new BusinessException("Kategori wajib dipilih.");
        }
        write(conn -> {
            Product product = productRepo.findByIdForUpdate(conn, id)
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            product.setName(nama);
            product.setCategoryId(categoryId);
            product.setPrice(price);
            productRepo.update(conn, product);
            return null;
        });
    }

    /* Mengaktifkan atau menonaktifkan produk (tidak menghapus data). */
    public void setActive(int id, boolean active) {
        write(conn -> {
            Product product = productRepo.findByIdForUpdate(conn, id)
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            product.setActive(active);
            productRepo.update(conn, product);
            return null;
        });
    }

    /* Menambah stok (RESTOCK). Jumlah harus lebih dari 0. */
    public void restock(int id, int quantity, String note) {
        if (quantity <= 0) {
            throw new BusinessException("Jumlah restok harus lebih dari 0.");
        }
        write(conn -> {
            productRepo.findByIdForUpdate(conn, id)
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            productRepo.updateStock(conn, id, quantity);
            movementRepo.insert(conn, movement(id, quantity, StockReason.RESTOCK,
                    blankToDefault(note, "Restok")));
            return null;
        });
    }

    /* Koreksi stok (ADJUSTMENT). Jumlah boleh negatif, tetapi stok tidak boleh minus. */
    public void adjustStock(int id, int delta, String note) {
        if (delta == 0) {
            throw new BusinessException("Jumlah penyesuaian tidak boleh 0.");
        }
        String catatan = required(note, "Catatan penyesuaian wajib diisi.");
        write(conn -> {
            productRepo.findByIdForUpdate(conn, id)
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            if (!productRepo.updateStock(conn, id, delta)) {
                throw new BusinessException("Penyesuaian dibatalkan: stok tidak boleh negatif.");
            }
            movementRepo.insert(conn, movement(id, delta, StockReason.ADJUSTMENT, catatan));
            return null;
        });
    }

    // ---------------------------------------------------------------- helper

    private static StockMovement movement(int productId, int change, StockReason reason, String note) {
        StockMovement m = new StockMovement();
        m.setProductId(productId);
        m.setOrderId(null);
        m.setChangeQty(change);
        m.setReason(reason);
        m.setNote(note);
        return m;
    }

    private static String validName(String name) {
        String nama = required(name, "Nama produk tidak boleh kosong.");
        if (nama.length() > MAX_NAME) {
            throw new BusinessException("Nama produk maksimal " + MAX_NAME + " karakter.");
        }
        return nama;
    }

    private static void validPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("Harga harus lebih dari 0.");
        }
    }

    private static String required(String text, String message) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new BusinessException(message);
        }
        return value;
    }

    private static String blankToNull(String text, int maxLength) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        if (value.length() > maxLength) {
            throw new BusinessException("Deskripsi maksimal " + maxLength + " karakter.");
        }
        return value;
    }

    private static String blankToDefault(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    private static <T> T call(SqlSupplier<T> work) {
        try {
            return work.get();
        } catch (SQLException e) {
            throw new BusinessException("Terjadi kesalahan database: " + e.getMessage(), e);
        }
    }

    private static <T> T write(DatabaseUtil.TxWork<T> work) {
        try {
            return DatabaseUtil.inTransaction(work);
        } catch (SQLException e) {
            throw new BusinessException("Terjadi kesalahan database: " + e.getMessage(), e);
        }
    }
}
