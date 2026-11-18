package com.github.rotibakarasjad.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OrderCodeGeneratorTest {

    @Test
    void generateFormatBenar() {
        assertEquals("RBA-20261103-0001",
                OrderCodeGenerator.generate(LocalDate.of(2026, 11, 3), 1));
        assertEquals("RBA-20261103-0042",
                OrderCodeGenerator.generate(LocalDate.of(2026, 11, 3), 42));
        assertEquals("RBA-20261103-9999",
                OrderCodeGenerator.generate(LocalDate.of(2026, 11, 3), 9999));
    }

    @Test
    void nomorUrutDiluarBatasDitolak() {
        LocalDate hari = LocalDate.of(2026, 11, 3);
        assertThrows(IllegalArgumentException.class, () -> OrderCodeGenerator.generate(hari, 0));
        assertThrows(IllegalArgumentException.class, () -> OrderCodeGenerator.generate(hari, 10000));
    }

    @Test
    void prefixForSesuaiDenganGenerate() {
        LocalDate hari = LocalDate.of(2026, 11, 3);
        assertEquals("RBA-20261103-", OrderCodeGenerator.prefixFor(hari));
        assertEquals(OrderCodeGenerator.prefixFor(hari) + "0007",
                OrderCodeGenerator.generate(hari, 7));
    }
}