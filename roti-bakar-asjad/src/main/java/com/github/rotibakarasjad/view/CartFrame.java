package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import com.github.rotibakarasjad.model.OrderItemRow;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class CartFrame extends JFrame {

    // DATA DUMMY. Diganti hasil OrderService.getOpenOrder di minggu 2.
    private final List<OrderItemRow> items = new ArrayList<>(List.of(
            new OrderItemRow(1, 1, "Roti Bakar Coklat Keju", new BigDecimal("15000"), 2),
            new OrderItemRow(2, 5, "Es Teh Manis", new BigDecimal("5000"), 1)));

    private BigDecimal total = BigDecimal.ZERO;

    public CartFrame() {
        initComponents();
        paidAmountField.setText("");
        initTable();
        initPaymentCombo();
        initEvents();
        refreshView();
    }
    
    private void initTable() {
        cartTable.setModel(new DefaultTableModel(
                new Object[]{"ID", "Produk", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

        private void initPaymentCombo() {
        String[] names = java.util.Arrays.stream(PaymentMethod.values())
                .map(Enum::name).toArray(String[]::new);
        paymentMethodCombo.setModel(new javax.swing.DefaultComboBoxModel<>(names));
    }

    private void initEvents() {
        payBtn.addActionListener(e -> payDummy());
        removeItemBtn.addActionListener(e -> removeItemDummy());
        cancelOrderBtn.addActionListener(e -> cancelDummy());

        backBtn.addActionListener(e -> {
            MainFrame main = new MainFrame();
            main.setLocationRelativeTo(null);
            main.setVisible(true);
            this.dispose();
        });
    }

    // Menyegarkan tabel, total, dan kembalian dari daftar item
    private void refreshView() {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        model.setRowCount(0);

        total = BigDecimal.ZERO;
        for (OrderItemRow item : items) {
            model.addRow(new Object[]{
                item.orderItemId(),
                item.productName(),
                CurrencyFormatter.format(item.unitPrice()),
                item.quantity(),
                CurrencyFormatter.format(item.subtotal())
            });
            total = total.add(item.subtotal());
        }

        totalLabel.setText(CurrencyFormatter.format(total));
        changeLabel.setText("-");
    }

    // Validasi uang dan hitung kembalian. Penyimpanan ke database
    // dikerjakan OrderService.payOrder di minggu 2.
    private void payDummy() {
        if (items.isEmpty()) {
            showError("Keranjang masih kosong.");
            return;
        }

        BigDecimal paid;
        try {
            paid = CurrencyFormatter.parse(paidAmountField.getText());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        if (paid.compareTo(total) < 0) {
            showError("Uang yang dibayarkan kurang dari total.");
            return;
        }

        BigDecimal change = paid.subtract(total);
        changeLabel.setText(CurrencyFormatter.format(change));

        // TODO minggu 2: orderService.payOrder(orderId, method, paid);
        JOptionPane.showMessageDialog(this,
                "(Dummy) Pembayaran " + paymentMethodCombo.getSelectedItem()
                + " diterima. Kembalian: " + CurrencyFormatter.format(change));
    }

    private void removeItemDummy() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            showError("Pilih item yang ingin dihapus.");
            return;
        }
        // TODO minggu 2: orderService.removeItem(orderItemId)
        items.remove(row);
        refreshView();
    }

    private void cancelDummy() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Batalkan pesanan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        // TODO minggu 2: orderService.cancelOrder(orderId)
        items.clear();
        refreshView();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        cartTable = new javax.swing.JTable();
        totalLabel = new javax.swing.JLabel();
        paymentMethodCombo = new javax.swing.JComboBox<>();
        paidAmountField = new javax.swing.JTextField();
        changeLabel = new javax.swing.JLabel();
        removeItemBtn = new javax.swing.JButton();
        cancelOrderBtn = new javax.swing.JButton();
        payBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        cartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Produk", "Harga", "Quantity", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(cartTable);

        totalLabel.setText("Total Tagihan");

        paymentMethodCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        paymentMethodCombo.setToolTipText("");
        paymentMethodCombo.addActionListener(this::paymentMethodComboActionPerformed);

        paidAmountField.setText("Uang Diterima");
        paidAmountField.addActionListener(this::paidAmountFieldActionPerformed);

        changeLabel.setText("Kembalian");

        removeItemBtn.setText("Hapus");

        cancelOrderBtn.setText("Batal");

        payBtn.setText("Bayar");

        backBtn.setText("Kembali");
        backBtn.addActionListener(this::backBtnActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(paidAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paymentMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removeItemBtn)
                            .addComponent(cancelOrderBtn)
                            .addComponent(payBtn)
                            .addComponent(backBtn)))
                    .addComponent(changeLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paidAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(paymentMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeItemBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelOrderBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(payBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(changeLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_backBtnActionPerformed

    private void paymentMethodComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paymentMethodComboActionPerformed

    private void paidAmountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paidAmountFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paidAmountFieldActionPerformed

public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(() -> {
            CartFrame f = new CartFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
        }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JButton cancelOrderBtn;
    private javax.swing.JTable cartTable;
    private javax.swing.JLabel changeLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField paidAmountField;
    private javax.swing.JButton payBtn;
    private javax.swing.JComboBox<String> paymentMethodCombo;
    private javax.swing.JButton removeItemBtn;
    private javax.swing.JLabel totalLabel;
    // End of variables declaration//GEN-END:variables
}
