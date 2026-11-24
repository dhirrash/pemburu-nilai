package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.repository.ProductRepository;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductRepository productRepo = new ProductRepository();

    /** Produk aktif untuk katalog kasir. Error database dibungkus BusinessException. */
    public List<Product> getActiveProducts() {
        try {
            return productRepo.findActive();
        } catch (SQLException e) {
            throw new BusinessException("Terjadi kesalahan database: " + e.getMessage(), e);
        }
    }

    // ---- method lama (masih stub). Dikerjakan orang B minggu 2. ----
    public void saveProduct(String name, String category, double price, int initialStock) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Nama produk tidak boleh kosong!");
        }
        if (price < 0) {
            throw new Exception("Harga produk tidak boleh bernilai negatif!");
        }
        if (initialStock < 0) {
            throw new Exception("Stok awal tidak boleh negatif!");
        }
        System.out.println("Produk " + name + " berhasil disimpan dengan stok awal " + initialStock);
    }

    public void adjustStock(int productId, int qtyChange, String reason, String note) throws Exception {
        if (qtyChange == 0) {
            throw new Exception("Jumlah perubahan stok tidak boleh 0!");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new Exception("Alasan perubahan stok (RESTOCK/ADJUSTMENT) wajib diisi!");
        }
        System.out.println("Stok produk ID " + productId + " diubah sebesar " + qtyChange + " (" + reason + ")");
    }
}