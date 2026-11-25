package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.model.OrderHistoryRow;
import com.github.rotibakarasjad.model.OrderItemRow;
import com.github.rotibakarasjad.service.OrderService;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Riwayat pesanan yang sudah dibayar. Filter tanggal, dan klik dua kali baris untuk melihat item.
 */
public class OrderHistoryFrame extends JFrame {

    private static final DateTimeFormatter DATE_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_OUTPUT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // batas awal dan akhir jika kolom tanggal dikosongkan
    private static final LocalDate AWAL_DATA = LocalDate.of(2000, 1, 1);

    private final OrderService orderService = new OrderService();

    // baris riwayat yang sedang ditampilkan, urutannya sama dengan baris tabel
    private List<OrderHistoryRow> rows = List.of();

    public OrderHistoryFrame() {
        initComponents();
        dateFromField.setText("");
        dateToField.setText("");
        initTable();
        initEvents();
        applyFilter();
    }

    private void initTable() {
        historyTable.setModel(new DefaultTableModel(
                new Object[]{"Kode", "Kasir", "Total", "Bayar", "Kembalian", "Metode", "Waktu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void initEvents() {
        filterBtn.addActionListener(e -> applyFilter());
        backBtn.addActionListener(e -> {
            MainFrame main = new MainFrame();
            main.setLocationRelativeTo(null);
            main.setVisible(true);
            dispose();
        });
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    tampilkanDetail();
                }
            }
        });
    }

    // ================= handler dari Form Editor (isi di sini) =================

    private void dateFromFieldActionPerformed(java.awt.event.ActionEvent evt) {
        applyFilter();
    }

    // ================= logika =================

    /** Mengambil riwayat dari service. Kolom kosong berarti tidak dibatasi. */
    private void applyFilter() {
        try {
            LocalDate from = parseDate(dateFromField.getText(), AWAL_DATA);
            LocalDate to = parseDate(dateToField.getText(), LocalDate.now());
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("Tanggal awal tidak boleh setelah tanggal akhir.");
            }
            fillTable(orderService.getHistory(from, to));
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    private static LocalDate parseDate(String text, LocalDate defaultValue) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(trimmed, DATE_INPUT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Format tanggal harus yyyy-MM-dd, contoh: 2026-11-03.");
        }
    }

    private void fillTable(List<OrderHistoryRow> hasil) {
        rows = List.copyOf(hasil);
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        for (OrderHistoryRow r : rows) {
            model.addRow(new Object[]{
                r.orderCode(),
                r.cashierName(),
                CurrencyFormatter.format(r.total()),
                CurrencyFormatter.format(r.paid()),
                CurrencyFormatter.format(r.change()),
                r.paymentMethod(),
                r.paidAt().format(TIME_OUTPUT)
            });
        }
    }

    /** Dialog berisi item pesanan dari baris riwayat yang dipilih (snapshot harga saat dipesan). */
    private void tampilkanDetail() {
        int viewRow = historyTable.getSelectedRow();
        if (viewRow < 0) {
            Dialogs.error(this, "Pilih riwayat transaksi terlebih dahulu.");
            return;
        }

        OrderHistoryRow r = rows.get(historyTable.convertRowIndexToModel(viewRow));
        try {
            List<OrderItemRow> items = orderService.getItems(r.orderId());

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Produk", "Harga", "Qty", "Subtotal"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (OrderItemRow item : items) {
                model.addRow(new Object[]{
                    item.productName(),
                    CurrencyFormatter.format(item.unitPrice()),
                    item.quantity(),
                    CurrencyFormatter.format(item.subtotal())
                });
            }

            JScrollPane scroll = new JScrollPane(new JTable(model));
            scroll.setPreferredSize(new Dimension(460, 220));

            String ringkasan = "Kode: " + r.orderCode() + "\n"
                    + "Kasir: " + r.cashierName() + "\n"
                    + "Metode: " + r.paymentMethod() + "\n"
                    + "Total: " + CurrencyFormatter.format(r.total())
                    + "   Bayar: " + CurrencyFormatter.format(r.paid())
                    + "   Kembalian: " + CurrencyFormatter.format(r.change());

            JOptionPane.showMessageDialog(this, new Object[]{ringkasan, scroll},
                    "Detail " + r.orderCode(), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            Dialogs.handle(this, ex);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new OrderHistoryFrame().setVisible(true));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        dateFromField = new javax.swing.JTextField();
        dateToField = new javax.swing.JTextField();
        filterBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Kode", "Kasir", "Total", "Bayar", "Kembalian", "Metode", "Waktu"
            }
        ));
        jScrollPane1.setViewportView(historyTable);

        dateFromField.setText("tgl");
        dateFromField.addActionListener(this::dateFromFieldActionPerformed);

        dateToField.setText("tgl");

        filterBtn.setText("Tampilkan");

        backBtn.setText("Kembali");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filterBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(backBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 515, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateFromField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateToField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 173, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dateFromField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dateToField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterBtn)
                    .addComponent(backBtn))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JTextField dateFromField;
    private javax.swing.JTextField dateToField;
    private javax.swing.JButton filterBtn;
    private javax.swing.JTable historyTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
