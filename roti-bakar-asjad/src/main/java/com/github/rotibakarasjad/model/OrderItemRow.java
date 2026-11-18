package com.github.rotibakarasjad.model;

import java.math.BigDecimal;

public record OrderItemRow(
        int orderItemId,
        int productId,
        String productName,
        BigDecimal unitPrice,
        int quantity) {

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}