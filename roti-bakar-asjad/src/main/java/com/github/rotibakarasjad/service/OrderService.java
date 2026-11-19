package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.Order;
import com.github.rotibakarasjad.entity.OrderItem;
import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.entity.StockMovement;
import com.github.rotibakarasjad.entity.enums.OrderStatus;
import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import com.github.rotibakarasjad.entity.enums.StockReason;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.model.OrderHistoryRow;
import com.github.rotibakarasjad.model.OrderItemRow;
import com.github.rotibakarasjad.repository.DatabaseUtil;
import com.github.rotibakarasjad.repository.OrderItemRepository;
import com.github.rotibakarasjad.repository.OrderRepository;
import com.github.rotibakarasjad.repository.ProductRepository;
import com.github.rotibakarasjad.repository.StockMovementRepository;
import com.github.rotibakarasjad.util.CurrencyFormatter;
import com.github.rotibakarasjad.util.OrderCodeGenerator;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Semua aturan transaksi. View hanya memanggil class ini.
 * Semua kesalahan aturan bisnis dilempar sebagai BusinessException (pesan siap tampil).
 * Error database juga dibungkus BusinessException("Terjadi kesalahan database...").
 */
public class OrderService {

    private final OrderRepository orderRepo = new OrderRepository();
    private final OrderItemRepository itemRepo = new OrderItemRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final StockMovementRepository movementRepo = new StockMovementRepository();

    /** Pesanan OPEN milik kasir; dibuat baru jika belum ada. */
    public Order getOrCreateOpenOrder(int userId) {
        return run(conn -> {
            var existing = orderRepo.findOpenOrderByUser(conn, userId);
            if (existing.isPresent()) {
                return existing.get();
            }
            Order order = new Order();
            order.setUserId(userId);
            String prefix = OrderCodeGenerator.prefixFor(LocalDate.now());
            int next = orderRepo.findLastSequence(conn, prefix) + 1;
            order.setOrderCode(OrderCodeGenerator.generate(LocalDate.now(), next));
            orderRepo.insert(conn, order);
            return orderRepo.findById(conn, order.getId()).orElseThrow();
        });
    }

    public Order getOrder(int orderId) {
        return run(conn -> orderRepo.findById(conn, orderId)
                .orElseThrow(() -> new BusinessException("Pesanan tidak ditemukan.")));
    }

    public List<OrderItemRow> getItems(int orderId) {
        return run(conn -> itemRepo.findRowsByOrder(conn, orderId));
    }

    /** Tambah produk ke pesanan. Jika produk sudah ada, kuantitas ditambah. */
    public void addItem(int orderId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Kuantitas harus lebih dari 0.");
        }
        run(conn -> {
            requireOpen(lockOrder(conn, orderId));
            Product product = productRepo.findByIdForUpdate(conn, productId)
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            if (!product.isActive()) {
                throw new BusinessException("Produk \"" + product.getName() + "\" sudah tidak dijual.");
            }

            var existing = itemRepo.findByOrderAndProduct(conn, orderId, productId);
            int newQty = quantity + existing.map(OrderItem::getQuantity).orElse(0);
            checkStock(product, newQty);

            if (existing.isPresent()) {
                itemRepo.updateQuantity(conn, existing.get().getId(), newQty);
            } else {
                OrderItem item = new OrderItem();
                item.setOrderId(orderId);
                item.setProductId(productId);
                item.setProductName(product.getName());   // dikunci saat ditambahkan
                item.setUnitPrice(product.getPrice());    // dikunci saat ditambahkan
                item.setQuantity(quantity);
                itemRepo.insert(conn, item);
            }
            orderRepo.recalculateTotal(conn, orderId);
            return null;
        });
    }

    /** Ubah kuantitas item. Untuk menghapus gunakan removeItem. */
    public void updateItemQuantity(int orderItemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new BusinessException("Kuantitas harus lebih dari 0. Untuk menghapus, gunakan hapus item.");
        }
        run(conn -> {
            OrderItem item = itemRepo.findById(conn, orderItemId)
                    .orElseThrow(() -> new BusinessException("Item pesanan tidak ditemukan."));
            requireOpen(lockOrder(conn, item.getOrderId()));
            Product product = productRepo.findByIdForUpdate(conn, item.getProductId())
                    .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
            if (!product.isActive()) {
                throw new BusinessException("Produk \"" + product.getName() + "\" sudah tidak dijual.");
            }
            checkStock(product, newQuantity);
            itemRepo.updateQuantity(conn, orderItemId, newQuantity);
            orderRepo.recalculateTotal(conn, item.getOrderId());
            return null;
        });
    }

    public void removeItem(int orderItemId) {
        run(conn -> {
            OrderItem item = itemRepo.findById(conn, orderItemId)
                    .orElseThrow(() -> new BusinessException("Item pesanan tidak ditemukan."));
            requireOpen(lockOrder(conn, item.getOrderId()));
            itemRepo.delete(conn, orderItemId);
            orderRepo.recalculateTotal(conn, item.getOrderId());
            return null;
        });
    }

    /** Membatalkan pesanan OPEN. Stok tidak berubah karena belum pernah dikurangi. */
    public void cancelOrder(int orderId) {
        run(conn -> {
            requireOpen(lockOrder(conn, orderId));
            orderRepo.updateStatus(conn, orderId, OrderStatus.CANCELLED);
            return null;
        });
    }

    /**
     * Pembayaran dalam SATU transaksi database:
     * validasi, cek ulang stok, kurangi stok, catat stock_movements, ubah status PAID.
     * Jika satu langkah gagal, semuanya dibatalkan (rollback).
     *
     * @return pesanan yang sudah PAID (lihat getChangeAmount() untuk kembalian)
     */
    public Order payOrder(int orderId, PaymentMethod method, BigDecimal paidAmount) {
        if (method == null) {
            throw new BusinessException("Metode pembayaran wajib dipilih.");
        }
        if (paidAmount == null || paidAmount.signum() < 0) {
            throw new BusinessException("Jumlah uang tidak valid.");
        }
        return run(conn -> {
            Order order = lockOrder(conn, orderId);
            requireOpen(order);

            List<OrderItem> items = itemRepo.findByOrder(conn, orderId);   // urut product_id
            if (items.isEmpty()) {
                throw new BusinessException("Pesanan kosong, tidak bisa dibayar.");
            }

            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem item : items) {
                total = total.add(item.getSubtotal());
            }
            if (paidAmount.compareTo(total) < 0) {
                throw new BusinessException("Uang kurang. Total " + CurrencyFormatter.format(total)
                        + ", dibayar " + CurrencyFormatter.format(paidAmount) + ".");
            }

            // Cek ulang stok (dengan kunci baris) lalu kurangi stok + catat pergerakan.
            for (OrderItem item : items) {
                Product product = productRepo.findByIdForUpdate(conn, item.getProductId())
                        .orElseThrow(() -> new BusinessException("Produk tidak ditemukan."));
                if (!product.isActive()) {
                    throw new BusinessException("Produk \"" + item.getProductName()
                            + "\" sudah tidak dijual. Hapus dari pesanan.");
                }
                checkStock(product, item.getQuantity());

                if (!productRepo.updateStock(conn, item.getProductId(), -item.getQuantity())) {
                    throw new BusinessException("Stok \"" + item.getProductName() + "\" tidak cukup.");
                }

                StockMovement m = new StockMovement();
                m.setProductId(item.getProductId());
                m.setOrderId(orderId);
                m.setChangeQty(-item.getQuantity());
                m.setReason(StockReason.SALE);
                m.setNote(order.getOrderCode());
                movementRepo.insert(conn, m);
            }

            BigDecimal change = paidAmount.subtract(total);
            orderRepo.markPaid(conn, orderId, total, paidAmount, change, method);
            return orderRepo.findById(conn, orderId).orElseThrow();
        });
    }

    /** Riwayat pesanan PAID dari tanggal from sampai to (keduanya inklusif). */
    public List<OrderHistoryRow> getHistory(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "Tanggal awal wajib diisi.");
        Objects.requireNonNull(to, "Tanggal akhir wajib diisi.");
        if (to.isBefore(from)) {
            throw new BusinessException("Tanggal akhir tidak boleh sebelum tanggal awal.");
        }
        return run(conn -> orderRepo.findPaidBetween(conn,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    // ---------------------------------------------------------------- helper

    private Order lockOrder(Connection conn, int orderId) throws SQLException {
        return orderRepo.findByIdForUpdate(conn, orderId)
                .orElseThrow(() -> new BusinessException("Pesanan tidak ditemukan."));
    }

    private void requireOpen(Order order) {
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException("Pesanan sudah " + order.getStatus() + " dan tidak bisa diubah.");
        }
    }

    private void checkStock(Product product, int wantedQty) {
        if (wantedQty > product.getStock()) {
            throw new BusinessException("Stok \"" + product.getName() + "\" tidak cukup. Sisa stok: "
                    + product.getStock() + ", diminta: " + wantedQty + ".");
        }
    }

    /** Menjalankan work dalam transaksi; SQLException dibungkus BusinessException. */
    private <T> T run(DatabaseUtil.TxWork<T> work) {
        try {
            return DatabaseUtil.inTransaction(work);
        } catch (SQLException e) {
            throw new BusinessException("Terjadi kesalahan database: " + e.getMessage(), e);
        }
    }
}
