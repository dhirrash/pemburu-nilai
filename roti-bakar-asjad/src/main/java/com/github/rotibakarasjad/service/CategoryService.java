/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.rotibakarasjad.service;

/**
 *
 * @author HP ELTEBOOKK
 */
public class CategoryService {
    public void addCategory(String categoryName) throws Exception {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new Exception("Nama kategori tidak boleh kosong!");
        }
        // Nanti dihubungkan ke CategoryRepository
        System.out.println("Kategori berhasil ditambahkan: " + categoryName);
    }
    
    public void deleteCategory(int categoryId, boolean isUsedByProduct) throws Exception {
        if (isUsedByProduct) {
            throw new Exception("Kategori tidak dapat dihapus karena masih digunakan oleh produk!");
        }
        System.out.println("Kategori dengan ID " + categoryId + " berhasil dihapus.");
    }
}
