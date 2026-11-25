/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.entity.Category;
import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.entity.enums.Role;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.service.CategoryService;
import com.github.rotibakarasjad.service.ProductService;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import com.github.rotibakarasjad.util.Session;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Panel admin: kelola produk, kategori, dan stok dalam satu layar (mode).
 */
public class AdminFrame extends javax.swing.JFrame {

    private static final String PRODUK = "PRODUK";
    private static final String KATEGORI = "KATEGORI";
    private static final String STOK = "STOK";

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(AdminFrame.class.getName());

    private String modeAktif = PRODUK;
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    public AdminFrame() {
        initComponents();
        if (!Session.isLoggedIn() || Session.getCurrentUser().getRole() != Role.ADMIN) {
            JOptionPane.showMessageDialog(this, "Akses ditolak! Anda bukan Admin.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }
        setTitle("Roti Bakar Asjad - Panel Admin (" + Session.getCurrentUser().getFullName() + ")");
        setLocationRelativeTo(null);
        initEvents();
        aturTampilanMode(PRODUK);
    }

    private void initEvents() {
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiFormDariBaris();
            }
        });
        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
    }

    // ================= handler dari Form Editor (isi di sini) =================

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {
        Session.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void btnNavProdukActionPerformed(java.awt.event.ActionEvent evt) {
        aturTampilanMode(PRODUK);
    }

    private void btnNavKategoriActionPerformed(java.awt.event.ActionEvent evt) {
        aturTampilanMode(KATEGORI);
    }

    private void btnNavStokActionPerformed(java.awt.event.ActionEvent evt) {
        aturTampilanMode(STOK);
    }

    private void txtInput1ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtInput2ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtInput3ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    // ================= tampilan mode =================

    /** Mengatur label, tombol, dan kolom input sesuai mode, lalu memuat tabel. */
    private void aturTampilanMode(String mode) {
        modeAktif = mode;
        kosongkanForm();

        switch (mode) {
            case PRODUK -> {
                lblMode.setText("MODE: KELOLA PRODUK");
                lblInput1.setText("Nama Produk :");
                lblInput2.setText("Kategori :");
                lblInput3.setText("Harga (Rp) :");
                tampilkanInput(true, true, true);
                btnSimpan.setText("Tambah Produk");
                btnUbah.setText("Simpan Perubahan");
                btnHapus.setText("Ubah Status");
            }
            case KATEGORI -> {
                lblMode.setText("MODE: KELOLA KATEGORI");
                lblInput1.setText("Nama Kategori :");
                tampilkanInput(true, false, false);
                btnSimpan.setText("Tambah Kategori");
                btnUbah.setText("Ubah Nama");
                btnHapus.setText("Hapus Kategori");
            }
            case STOK -> {
                lblMode.setText("MODE: RESTOK & PENYESUAIAN STOK");
                lblInput1.setText("Produk :");
                lblInput2.setText("Catatan :");
                lblInput3.setText("Jumlah (+/-) :");
                tampilkanInput(true, true, true);
                txtInput1.setEditable(false);
                btnSimpan.setText("Restok (+)");
                btnUbah.setText("Penyesuaian +/-");
                btnHapus.setText("Bersihkan Form");
            }
            default -> throw new IllegalArgumentException("Mode tidak dikenal: " + mode);
        }
        btnUbah.setVisible(true);
        btnHapus.setVisible(true);
        muatTabel();
    }

    private void tampilkanInput(boolean input1, boolean input2, boolean input3) {
        lblInput1.setVisible(input1);
        txtInput1.setVisible(input1);
        lblInput2.setVisible(input2);
        txtInput2.setVisible(input2);
        lblInput3.setVisible(input3);
        txtInput3.setVisible(input3);
    }

    private void kosongkanForm() {
        txtInput1.setText("");
        txtInput2.setText("");
        txtInput3.setText("");
        txtInput1.setEditable(true);
    }

    /** Mengisi tabel dari service sesuai mode. Kolom 0 selalu berisi ID. */
    private void muatTabel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        try {
            switch (modeAktif) {
                case PRODUK -> {
                    model.setColumnIdentifiers(new Object[]{"ID", "Nama Produk", "Kategori",
                        "Harga", "Stok", "Status"});
                    for (Product p : productService.getAllProducts()) {
                        model.addRow(new Object[]{
                            p.getId(),
                            p.getName(),
                            p.getCategoryName(),
                            CurrencyFormatter.format(p.getPrice()),
                            p.getStock(),
                            p.isActive() ? "Aktif" : "Nonaktif"
                        });
                    }
                }
                case KATEGORI -> {
                    model.setColumnIdentifiers(new Object[]{"ID", "Nama Kategori", "Jumlah Produk"});
                    for (CategoryService.CategoryUsage c : categoryService.getAllWithUsage()) {
                        model.addRow(new Object[]{c.id(), c.name(), c.productCount()});
                    }
                }
                case STOK -> {
                    model.setColumnIdentifiers(new Object[]{"ID", "Nama Produk", "Stok Saat Ini"});
                    for (Product p : productService.getAllProducts()) {
                        model.addRow(new Object[]{p.getId(), p.getName(), p.getStock()});
                    }
                }
                default -> {
                }
            }
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
        tblData.setModel(model);
    }

    /** Baris dipilih -> isi form (untuk diubah). */
    private void isiFormDariBaris() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            return;
        }
        switch (modeAktif) {
            case PRODUK -> {
                txtInput1.setText(String.valueOf(tblData.getValueAt(row, 1)));
                txtInput2.setText(String.valueOf(tblData.getValueAt(row, 2)));
                txtInput3.setText(String.valueOf(tblData.getValueAt(row, 3)));
            }
            case KATEGORI, STOK -> txtInput1.setText(String.valueOf(tblData.getValueAt(row, 1)));
            default -> {
            }
        }
    }

    // ================= aksi tombol =================

    private void simpan() {
        try {
            switch (modeAktif) {
                case PRODUK -> {
                    productService.createProduct(txtInput1.getText(), null,
                            categoryIdByName(txtInput2.getText()),
                            CurrencyFormatter.parse(txtInput3.getText()), 0);
                    Dialogs.info(this, "Produk ditambahkan. Isi stok awal lewat menu Restok & Stok.");
                }
                case KATEGORI -> {
                    categoryService.addCategory(txtInput1.getText());
                    Dialogs.info(this, "Kategori ditambahkan.");
                }
                case STOK -> {
                    productService.restock(selectedId(), parseBilangan(txtInput3.getText()),
                            txtInput2.getText());
                    Dialogs.info(this, "Stok berhasil ditambah.");
                }
                default -> {
                }
            }
            aturTampilanMode(modeAktif);
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void ubah() {
        try {
            switch (modeAktif) {
                case PRODUK -> {
                    productService.updateProduct(selectedId(), txtInput1.getText(),
                            categoryIdByName(txtInput2.getText()),
                            CurrencyFormatter.parse(txtInput3.getText()));
                    Dialogs.info(this, "Data produk diperbarui.");
                }
                case KATEGORI -> {
                    categoryService.renameCategory(selectedId(), txtInput1.getText());
                    Dialogs.info(this, "Nama kategori diperbarui.");
                }
                case STOK -> {
                    productService.adjustStock(selectedId(), parseBilangan(txtInput3.getText()),
                            txtInput2.getText());
                    Dialogs.info(this, "Stok berhasil disesuaikan.");
                }
                default -> {
                }
            }
            aturTampilanMode(modeAktif);
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void hapus() {
        try {
            switch (modeAktif) {
                case PRODUK -> {
                    boolean aktif = "Aktif".equals(tblData.getValueAt(selectedRow(), 5));
                    String aksi = aktif ? "menonaktifkan" : "mengaktifkan";
                    if (Dialogs.confirm(this, "Yakin ingin " + aksi + " produk ini?\n"
                            + "Produk nonaktif tidak tampil di katalog kasir.")) {
                        productService.setActive(selectedId(), !aktif);
                        aturTampilanMode(modeAktif);
                    }
                }
                case KATEGORI -> {
                    if (Dialogs.confirm(this, "Hapus kategori ini?")) {
                        categoryService.deleteCategory(selectedId());
                        aturTampilanMode(modeAktif);
                    }
                }
                case STOK -> aturTampilanMode(modeAktif);
                default -> {
                }
            }
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    // ================= helper =================

    private int selectedRow() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            throw new BusinessException("Pilih baris pada tabel terlebih dahulu.");
        }
        return row;
    }

    private int selectedId() {
        return (Integer) tblData.getValueAt(selectedRow(), 0);
    }

    /** Nama kategori dari form -> id kategori (tidak peka huruf besar/kecil). */
    private int categoryIdByName(String nama) {
        String dicari = nama == null ? "" : nama.trim();
        for (Category c : categoryService.findAll()) {
            if (c.getName().equalsIgnoreCase(dicari)) {
                return c.getId();
            }
        }
        throw new BusinessException("Kategori \"" + dicari
                + "\" tidak ditemukan. Tambahkan dulu di menu Kelola Kategori.");
    }

    /** Bilangan bulat, boleh negatif. */
    private static int parseBilangan(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException("Jumlah harus berupa bilangan bulat.");
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new AdminFrame().setVisible(true));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJudul = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnNavProduk = new javax.swing.JButton();
        btnNavKategori = new javax.swing.JButton();
        btnNavStok = new javax.swing.JButton();
        lblMode = new javax.swing.JLabel();
        lblInput1 = new javax.swing.JLabel();
        lblInput3 = new javax.swing.JLabel();
        lblInput2 = new javax.swing.JLabel();
        txtInput1 = new javax.swing.JTextField();
        txtInput2 = new javax.swing.JTextField();
        txtInput3 = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblJudul.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblJudul.setText("ADMIN - ROTI BAKAR ASJAD");

        btnLogout.setText("Logout");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        btnNavProduk.setText("Kelola Produk");
        btnNavProduk.addActionListener(this::btnNavProdukActionPerformed);

        btnNavKategori.setText("Kelola Kategori");
        btnNavKategori.addActionListener(this::btnNavKategoriActionPerformed);

        btnNavStok.setText("Restok & Stok");
        btnNavStok.addActionListener(this::btnNavStokActionPerformed);

        lblMode.setText("Manajemen Produk");

        lblInput1.setText("Nama Produk : ");

        lblInput3.setText("Harga / Qty : ");

        lblInput2.setText("Kategori : ");

        txtInput1.addActionListener(this::txtInput1ActionPerformed);

        txtInput2.addActionListener(this::txtInput2ActionPerformed);

        txtInput3.addActionListener(this::txtInput3ActionPerformed);

        btnSimpan.setText("SImpan");

        btnUbah.setText("Ubah");

        btnHapus.setText("Hapus");

        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblData);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblInput1)
                                    .addComponent(lblInput2)
                                    .addComponent(btnSimpan, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtInput2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(btnUbah)
                                        .addGap(81, 81, 81))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtInput1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(52, 52, 52)
                                        .addComponent(lblInput3)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInput3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblMode)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnLogout)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(17, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNavProduk)
                        .addGap(43, 43, 43)
                        .addComponent(btnNavKategori)
                        .addGap(32, 32, 32)
                        .addComponent(btnNavStok)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnHapus)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addComponent(lblJudul)
                        .addGap(97, 97, 97)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblJudul)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNavProduk)
                    .addComponent(btnNavKategori)
                    .addComponent(btnNavStok))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInput1)
                    .addComponent(lblInput3)
                    .addComponent(txtInput1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInput3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInput2)
                    .addComponent(txtInput2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnUbah)
                    .addComponent(btnHapus))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLogout)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavKategori;
    private javax.swing.JButton btnNavProduk;
    private javax.swing.JButton btnNavStok;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUbah;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblInput1;
    private javax.swing.JLabel lblInput2;
    private javax.swing.JLabel lblInput3;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblMode;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtInput1;
    private javax.swing.JTextField txtInput2;
    private javax.swing.JTextField txtInput3;
    // End of variables declaration//GEN-END:variables
}
