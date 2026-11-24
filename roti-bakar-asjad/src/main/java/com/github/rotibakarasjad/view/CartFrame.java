package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.entity.Order;
import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import com.github.rotibakarasjad.model.OrderItemRow;
import com.github.rotibakarasjad.service.OrderService;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import com.github.rotibakarasjad.util.Session;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Keranjang (pesanan OPEN) dan pembayaran kasir.
 */
public class CartFrame extends JFrame {

    private final OrderService orderService = new OrderService();

    private int orderId;
    private List<OrderItemRow> items = List.of();
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
                new Object[]{"ID Item", "Produk", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void initPaymentCombo() {
        String[] names = Arrays.stream(PaymentMethod.values())
                .map(Enum::name).toArray(String[]::new);
        paymentMethodCombo.setModel(new DefaultComboBoxModel<>(names));
    }

    private void initEvents() {
        payBtn.addActionListener(e -> pay());
        removeItemBtn.addActionListener(e -> removeItem());
        cancelOrderBtn.addActionListener(e -> cancelOrder());

        // klik dua kali baris untuk mengubah jumlah
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editQuantity();
                }
            }
        });
    }

    // ================= handler dari Form Editor (isi di sini) =================



    // ================= logika =================

    /** Mengambil pesanan OPEN kasir beserta itemnya lalu menggambar ulang layar. */
    private void refreshView() {
        try {
            orderId = orderService.getOrCreateOpenOrder(Session.requireUserId()).getId();
            items = orderService.getItems(orderId);

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
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void pay() {
        if (items.isEmpty()) {
            Dialogs.error(this, "Keranjang masih kosong.");
            return;
        }

        try {
            PaymentMethod method = PaymentMethod.valueOf(
                    String.valueOf(paymentMethodCombo.getSelectedItem()));
            BigDecimal paid = CurrencyFormatter.parse(paidAmountField.getText());

            Order paidOrder = orderService.payOrder(orderId, method, paid);

            Dialogs.info(this, "Pembayaran berhasil.\n"
                    + "Kode pesanan: " + paidOrder.getOrderCode() + "\n"
                    + "Total: " + CurrencyFormatter.format(paidOrder.getTotalAmount()) + "\n"
                    + "Kembalian: " + CurrencyFormatter.format(paidOrder.getChangeAmount()));

            paidAmountField.setText("");
            refreshView();   // pesanan lama sudah PAID, keranjang baru dibuat
            changeLabel.setText(CurrencyFormatter.format(paidOrder.getChangeAmount()));
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void removeItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            Dialogs.error(this, "Pilih item yang ingin dihapus.");
            return;
        }
        if (!Dialogs.confirm(this, "Hapus item ini dari pesanan?")) {
            return;
        }

        try {
            orderService.removeItem(selectedItem(row).orderItemId());
            refreshView();
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void editQuantity() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            Dialogs.error(this, "Pilih item yang ingin diubah jumlahnya.");
            return;
        }

        OrderItemRow item = selectedItem(row);
        String input = JOptionPane.showInputDialog(this,
                "Jumlah sekarang: " + item.quantity() + "\nMasukkan jumlah baru untuk "
                + item.productName() + ":",
                "Ubah Jumlah",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;   // dibatalkan
        }

        try {
            int quantity;
            try {
                quantity = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Jumlah harus berupa bilangan bulat.");
            }
            orderService.updateItemQuantity(item.orderItemId(), quantity);
            refreshView();
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private void cancelOrder() {
        if (!Dialogs.confirm(this, "Batalkan pesanan ini? Semua item akan dihapus dari keranjang.")) {
            return;
        }
        try {
            orderService.cancelOrder(orderId);
            refreshView();
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    /** Baris tabel dan urutan list items selalu sama, jadi indeks model dipakai langsung. */
    private OrderItemRow selectedItem(int viewRow) {
        return items.get(cartTable.convertRowIndexToModel(viewRow));
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            CartFrame f = new CartFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    // [blok initComponents() dari Form Editor, JANGAN diubah]

    // [deklarasi komponen // Variables declaration, JANGAN diubah]


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
        MainFrame main = new MainFrame();
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void paymentMethodComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodComboActionPerformed
        // metode pembayaran dibaca saat tombol Bayar ditekan
    }//GEN-LAST:event_paymentMethodComboActionPerformed

    private void paidAmountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paidAmountFieldActionPerformed
        pay();
    }//GEN-LAST:event_paidAmountFieldActionPerformed

    
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
