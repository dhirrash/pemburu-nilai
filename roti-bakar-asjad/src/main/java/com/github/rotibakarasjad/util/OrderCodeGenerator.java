package com.github.rotibakarasjad.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/*
 format RBA-YYYYMMDD-NNNN.
 Contoh: RBA-20261103-0001
 */
public final class OrderCodeGenerator {

    public static final int MAX_SEQUENCE = 9999;

    private static final String PREFIX = "RBA";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OrderCodeGenerator() {
    }

    public static String generate(LocalDate date, int sequence) {
        Objects.requireNonNull(date, "Tanggal tidak boleh null.");
        if (sequence < 1 || sequence > MAX_SEQUENCE) {
            throw new IllegalArgumentException("Nomor urut harus antara 1 dan " + MAX_SEQUENCE + ".");
        }
        return prefixFor(date) + String.format("%04d", sequence);
    }

    public static String prefixFor(LocalDate date) {
        Objects.requireNonNull(date, "Tanggal tidak boleh null.");
        return PREFIX + "-" + date.format(DATE_FORMAT) + "-";
    }
}