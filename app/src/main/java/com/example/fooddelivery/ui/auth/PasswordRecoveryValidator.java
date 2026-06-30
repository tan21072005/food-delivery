package com.example.fooddelivery.ui.auth;

import java.util.Locale;
import java.util.regex.Pattern;

public final class PasswordRecoveryValidator {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE);
    private PasswordRecoveryValidator() {}

    public static boolean isValidEmail(String value) {
        return value != null
                && EMAIL.matcher(value.trim()).matches()
                && suggestEmailCorrection(value) == null;
    }

    public static String suggestEmailCorrection(String value) {
        if (value == null) return null;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.lastIndexOf('@');
        if (separator <= 0 || separator == normalized.length() - 1) return null;

        String domain = normalized.substring(separator + 1);
        if ("gmail.co".equals(domain)
                || "gmai.com".equals(domain)
                || "gmial.com".equals(domain)) {
            return normalized.substring(0, separator + 1) + "gmail.com";
        }
        return null;
    }

    public static boolean isValidOtp(String value) {
        return value != null && value.matches("\\d{6}");
    }

    public static boolean isStrongPassword(String value) {
        return value != null && value.length() >= 8 && value.length() <= 20
                && value.matches(".*[A-Za-z].*")
                && value.matches(".*\\d.*")
                && value.matches(".*[^A-Za-z0-9].*");
    }
}
