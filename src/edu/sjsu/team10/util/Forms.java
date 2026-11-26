package edu.sjsu.team10.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Parsing helpers for form fields, shared by the catalog and restock servlets.
public final class Forms {

    // largest value a DECIMAL(10,2) column can hold
    public static final BigDecimal MAX_DECIMAL = new BigDecimal("99999999.99");

    private Forms() { }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static Integer parseId(String value) {
        try {
            return Integer.valueOf(trim(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // a number from 0 to MAX_DECIMAL rounded to 2 decimals, or null if invalid
    public static BigDecimal parseAmount(String value) {
        try {
            BigDecimal amount = new BigDecimal(trim(value)).setScale(2, RoundingMode.HALF_UP);
            if (amount.signum() < 0 || amount.compareTo(MAX_DECIMAL) > 0) {
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // "12.50" -> "12.5", "3.00" -> "3"
    public static String plain(BigDecimal value) {
        if (value == null) {
            return "";
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return (stripped.scale() < 0 ? stripped.setScale(0) : stripped).toPlainString();
    }

    // "4.5" -> "$4.50"
    public static String money(BigDecimal value) {
        return value == null ? "" : "$" + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
