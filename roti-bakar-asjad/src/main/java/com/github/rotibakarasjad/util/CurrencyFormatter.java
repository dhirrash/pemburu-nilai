package com.github.rotibakarasjad.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final Locale ID_LOCALE = Locale.of("id", "ID");

    private CurrencyFormatter() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "Rp 0";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(ID_LOCALE);
        return "Rp " + nf.format(amount.setScale(0, RoundingMode.HALF_UP));
    }

    /**
     * Mengubah teks input kasir menjadi BigDecimal.
     * Menerima: "15000", "15.000", "Rp 15.000".
     * Menolak: "1.5", "abc", kosong.
     */
    public static BigDecimal parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Nominal tidak boleh kosong.");
        }

        String cleaned = text.trim()
                .replaceFirst("(?i)^rp\\s*", "")
                .replace(" ", "");

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Nominal tidak boleh kosong.");
        }

        // hanya angka polos, atau angka dengan titik sebagai pemisah ribuan
        if (!cleaned.matches("\\d+|\\d{1,3}(\\.\\d{3})+")) {
            throw new IllegalArgumentException("Nominal harus berupa angka bulat, contoh: 15000 atau 15.000.");
        }

        return new BigDecimal(cleaned.replace(".", ""));
    }
}