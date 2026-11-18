package com.github.rotibakarasjad.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.rotibakarasjad.entity.Order;
import com.github.rotibakarasjad.entity.Product;
import com.github.rotibakarasjad.entity.enums.OrderStatus;
import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.repository.DatabaseUtil;
import com.github.rotibakarasjad.repository.ProductRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/* Memakai produk uji sendiri (stok 5, harga 10.000) supaya data seed tidak berubah. */
class OrderServiceTest {

    private static final int KASIR_ID = 2;   // kasir1 dari seed
    private final OrderService service = new OrderService();
    private final ProductRepository productRepo = new ProductRepository();
    private int productId;
    private int orderId;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection c = DatabaseUtil.getConnection()) {
            assumeTrue(c.isValid(2));
        } catch (Exception e) {
            assumeTrue(false, "Database tidak tersedia: " + e.getMessage());
        }
        // pesanan OPEN sisa uji sebelumnya dibatalkan agar tes bersih
        Order open = service.getOrCreateOpenOrder(KASIR_ID);
        service.cancelOrder(open.getId());

        productId = productRepo.insert(new Product(1, "TEST-" + System.nanoTime(), null,
                new BigDecimal("10000"), 5, true));
        orderId = service.getOrCreateOpenOrder(KASIR_ID).getId();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (productId == 0) {
            return;
        }
        try (Connection c = DatabaseUtil.getConnection()) {
            exec(c, "DELETE FROM stock_movements WHERE product_id = ?", productId);
            exec(c, "DELETE FROM order_items WHERE product_id = ?", productId);
            exec(c, "DELETE FROM orders WHERE id = ? AND status <> 'PAID' "
                  + "AND NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = ?)", orderId, orderId);
            exec(c, "DELETE FROM products WHERE id = ?", productId);
        }
    }

    @Test
    void kodePesananBerformatBenar() {
        String code = service.getOrder(orderId).getOrderCode();
        assertTrue(code.startsWith("RBA-" + LocalDate.now().toString().replace("-", "") + "-"), code);
    }

    @Test
    void addItemMenjumlahkanKuantitasDanTotal() {
        service.addItem(orderId, productId, 2);
        service.addItem(orderId, productId, 1);
        assertEquals(1, service.getItems(orderId).size());
        assertEquals(3, service.getItems(orderId).get(0).quantity());
        assertEquals(0, new BigDecimal("30000").compareTo(service.getOrder(orderId).getTotalAmount()));
    }

    @Test
    void addItemMelebihiStokDitolak() {
        assertThrows(BusinessException.class, () -> service.addItem(orderId, productId, 6));
    }

    @Test
    void updateDanRemoveItem() {
        service.addItem(orderId, productId, 1);
        int itemId = service.getItems(orderId).get(0).orderItemId();
        service.updateItemQuantity(itemId, 4);
        assertEquals(0, new BigDecimal("40000").compareTo(service.getOrder(orderId).getTotalAmount()));
        assertThrows(BusinessException.class, () -> service.updateItemQuantity(itemId, 6));
        service.removeItem(itemId);
        assertTrue(service.getItems(orderId).isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(service.getOrder(orderId).getTotalAmount()));
    }

    @Test
    void bayarUangKurangDitolakDanStokTetap() throws Exception {
        service.addItem(orderId, productId, 2);
        assertThrows(BusinessException.class,
                () -> service.payOrder(orderId, PaymentMethod.TUNAI, new BigDecimal("19999")));
        assertEquals(5, productRepo.findById(productId).orElseThrow().getStock());
        assertEquals(OrderStatus.OPEN, service.getOrder(orderId).getStatus());
    }

    @Test
    void bayarPesananKosongDitolak() {
        assertThrows(BusinessException.class,
                () -> service.payOrder(orderId, PaymentMethod.TUNAI, new BigDecimal("1000")));
    }

    @Test
    void bayarBerhasilKurangiStokDanCatatMovement() throws Exception {
        service.addItem(orderId, productId, 2);
        Order paid = service.payOrder(orderId, PaymentMethod.TUNAI, new BigDecimal("50000"));

        assertEquals(OrderStatus.PAID, paid.getStatus());
        assertEquals(0, new BigDecimal("30000").compareTo(paid.getChangeAmount()));
        assertEquals(3, productRepo.findById(productId).orElseThrow().getStock());
        assertEquals(1, new com.github.rotibakarasjad.repository.StockMovementRepository()
                .findByProduct(productId).size());
        assertTrue(service.getHistory(LocalDate.now(), LocalDate.now()).stream()
                .anyMatch(r -> r.orderId() == orderId));
        assertThrows(BusinessException.class, () -> service.addItem(orderId, productId, 1));
    }

    @Test
    void cancelOrderMembuatStatusCancelledDanStokTetap() throws Exception {
        service.addItem(orderId, productId, 2);
        service.cancelOrder(orderId);
        assertEquals(OrderStatus.CANCELLED, service.getOrder(orderId).getStatus());
        assertEquals(5, productRepo.findById(productId).orElseThrow().getStock());
    }

    private static void exec(Connection c, String sql, int... params) throws Exception {
        try (PreparedStatement st = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                st.setInt(i + 1, params[i]);
            }
            st.executeUpdate();
        }
    }
}
