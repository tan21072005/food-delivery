package com.example.fooddelivery.ui.auth;

import java.util.regex.Pattern;

public final class PasswordRecoveryValidator {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE);
    private PasswordRecoveryValidator() {}

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
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
