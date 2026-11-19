/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.rotibakarasjad.service;

/**
 *
 * @author HP ELTEBOOKK
 */
public class ProductService {
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
