package com.github.rotibakarasjad.model;

import com.github.rotibakarasjad.entity.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderHistoryRow(
        int orderId,
        String orderCode,
        String cashierName,
        BigDecimal total,
        BigDecimal paid,
        BigDecimal change,
        PaymentMethod paymentMethod,
        LocalDateTime paidAt) {
}