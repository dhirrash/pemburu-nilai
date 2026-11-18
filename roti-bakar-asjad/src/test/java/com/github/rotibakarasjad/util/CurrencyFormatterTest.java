package com.github.rotibakarasjad.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CurrencyFormatterTest {

    @Test
    void formatMenjadiRupiahDenganTitikRibuan() {
        assertEquals("Rp 15.000", CurrencyFormatter.format(new BigDecimal("15000")));
        assertEquals("Rp 1.500.000", CurrencyFormatter.format(new BigDecimal("1500000")));
        assertEquals("Rp 0", CurrencyFormatter.format(BigDecimal.ZERO));
    }

    @Test
    void formatNullMenjadiNol() {
        assertEquals("Rp 0", CurrencyFormatter.format(null));
    }

    @Test
    void parseAngkaPolos() {
        assertEquals(new BigDecimal("15000"), CurrencyFormatter.parse("15000"));
    }

    @Test
    void parseDenganTitikDanPrefixRp() {
        assertEquals(new BigDecimal("15000"), CurrencyFormatter.parse("15.000"));
        assertEquals(new BigDecimal("15000"), CurrencyFormatter.parse("Rp 15.000"));
        assertEquals(new BigDecimal("1500000"), CurrencyFormatter.parse("1.500.000"));
    }

    @Test
    void parseDitolakJikaBukanAngkaBulat() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.parse("1.5"));
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.parse(""));
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.parse(null));
    }
}