package com.magenta.cine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {

    @Test
    void requireNonBlankTrimmedShouldReturnTrimmedValue() {
        String value = Validators.requireNonBlankTrimmed("  Hola ", "Saludo");
        assertEquals("Hola", value);
    }

    @Test
    void requireNonBlankTrimmedShouldThrowOnBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validators.requireNonBlankTrimmed("  ", "Campo"));
        assertTrue(ex.getMessage().contains("Campo"));
    }

    @Test
    void parseIntShouldThrowWhenOutOfRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validators.parseInt("2025", "Año", 1900, 2024));
        assertTrue(ex.getMessage().contains("Año"));
    }

    @Test
    void parseIntShouldRejectNullInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validators.parseInt(null, "Año", 1900, 2100));
        assertEquals("El campo 'Año' debe ser numérico", ex.getMessage());
    }

    @Test
    void parseIntShouldParseValidValue() {
        assertEquals(1999, Validators.parseInt("1999", "Año", 1900, 2100));
    }
}
