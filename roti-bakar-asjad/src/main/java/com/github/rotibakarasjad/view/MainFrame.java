/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.service.OrderService;
import com.github.rotibakarasjad.service.ProductService;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import com.github.rotibakarasjad.util.Session;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author NADHIR
 */   
/**
 * Katalog produk kasir. Data produk dan pesanan diambil lewat service.
 */
public class MainFrame extends JFrame {

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    private List<Product> products = List.of();

    public MainFrame() {
        initComponents();
        initTable();
        initEvents();
        quantityField.setText("");
        loadProducts();
    }

    private void initTable() {
        productTable.setModel(new DefaultTableModel(
                new Object[]{"ID", "Nama Produk", "Kategori", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryFilter.setModel(new DefaultComboBoxModel<>(
                new String[]{"Semua", "Roti Bakar", "Minuman", "Snack"}));
    }

    private void initEvents() {
        categoryFilter.addActionListener(e -> applyFilter());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = productTable.getSelectedRow();
                selectedProductLabel.setText(row < 0
                        ? "-"
                        : String.valueOf(productTable.getValueAt(row, 1)));
            }
        });

        historyBtn.addActionListener(e -> openScreen(new OrderHistoryFrame()));
    }

    // ================= handler dari Form Editor (isi di sini) =================

    

    // ================= logika =================

    private void openScreen(JFrame next) {
        next.setLocationRelativeTo(null);
        next.setVisible(true);
        dispose();
    }

    /** Memuat produk aktif di background supaya jendela tidak macet. */
    private void loadProducts() {
        setTitle("Roti Bakar Asjad - Memuat produk...");

        new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() {
                return productService.getActiveProducts();
            }

            @Override
            protected void done() {
                try {
                    products = get();
                    applyFilter();
                    setTitle("Roti Bakar Asjad");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    setTitle("Roti Bakar Asjad - Gagal memuat");
                    Throwable cause = ex.getCause();
                    Dialogs.handle(MainFrame.this,
                            cause instanceof Exception e ? e : new IllegalStateException(String.valueOf(cause)));
                }
            }
        }.execute();
    }

    /** Mengisi tabel sesuai kategori yang dipilih. */
    private void applyFilter() {
        String selected = String.valueOf(categoryFilter.getSelectedItem());
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0);

        for (Product p : products) {
            if (selected.equals("Semua") || selected.equals(p.getCategoryName())) {
                model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategoryName(),
                    CurrencyFormatter.format(p.getPrice()),
                    p.getStock()
                });
            }
        }
    }

    /** Memasukkan produk terpilih ke pesanan OPEN kasir yang login. */
    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            Dialogs.error(this, "Pilih produk di tabel terlebih dahulu.");
            return;
        }

        try {
            int quantity = parseQuantity(quantityField.getText());
            int productId = (Integer) productTable.getValueAt(row, 0);

            int orderId = orderService.getOrCreateOpenOrder(Session.requireUserId()).getId();
            orderService.addItem(orderId, productId, quantity);

            quantityField.setText("");
            Dialogs.info(this, "Produk berhasil dimasukkan ke pesanan.");
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private static int parseQuantity(String text) {
        int quantity;
        try {
            quantity = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Kuantitas harus berupa bilangan bulat.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0.");
        }
        return quantity;
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    // [blok initComponents() dari Form Editor, JANGAN diubah]

    // [deklarasi komponen // Variables declaration, JANGAN diubah]

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoryFilter = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        productTable = new javax.swing.JTable();
        selectedProductLabel = new javax.swing.JLabel();
        quantityField = new javax.swing.JTextField();
        historyBtn = new javax.swing.JButton();
        cartBtn = new javax.swing.JButton();
        addToCartBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        categoryFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        productTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama Produk", "Kategori", "Harga", "Stok"
            }
        ));
        jScrollPane1.setViewportView(productTable);

        selectedProductLabel.setText("Produk Terpilih");

        quantityField.setText("Jumlah");
        quantityField.addActionListener(this::quantityFieldActionPerformed);

        historyBtn.setText("Histori");

        cartBtn.setText("Keranjang");
        cartBtn.addActionListener(this::cartBtnActionPerformed);

        addToCartBtn.setText("Masukkan");
        addToCartBtn.addActionListener(this::addToCartBtnActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addToCartBtn)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(selectedProductLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(quantityField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(categoryFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cartBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(historyBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(categoryFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(historyBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cartBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedProductLabel)
                    .addComponent(quantityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToCartBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void quantityFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityFieldActionPerformed
        addToCart();
    }//GEN-LAST:event_quantityFieldActionPerformed

    private void addToCartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToCartBtnActionPerformed
        addToCart();
    }//GEN-LAST:event_addToCartBtnActionPerformed

    private void cartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cartBtnActionPerformed
        openScreen(new CartFrame());
    }//GEN-LAST:event_cartBtnActionPerformed

    /**
     * @param args the command line arguments
     */
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addToCartBtn;
    private javax.swing.JButton cartBtn;
    private javax.swing.JComboBox<String> categoryFilter;
    private javax.swing.JButton historyBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable productTable;
    private javax.swing.JTextField quantityField;
    private javax.swing.JLabel selectedProductLabel;
    // End of variables declaration//GEN-END:variables
}