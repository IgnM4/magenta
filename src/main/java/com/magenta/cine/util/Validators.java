/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.magenta.cine.util;

/**
 *
 * @author ignac
 */

public class Validators {
    public static void requireNonBlank(String s, String field) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("El campo '" + field + "' es obligatorio");
        }
    }

    public static String requireNonBlankTrimmed(String s, String field) {
        requireNonBlank(s, field);
        return s.trim();
    }
    public static int parseInt(String s, String field, int min, int max) {
        String value = s == null ? "" : s.trim();
        try {
            int v = Integer.parseInt(value);
            if (v < min || v > max) {
                throw new IllegalArgumentException(
                    "El campo '" + field + "' debe estar entre " + min + " y " + max
                );
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + field + "' debe ser numérico");
        }
    }
}
