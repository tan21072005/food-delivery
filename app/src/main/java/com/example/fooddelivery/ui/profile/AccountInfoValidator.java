package com.example.fooddelivery.ui.profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AccountInfoValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    private AccountInfoValidator() {
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidBirthDate(String value, Date today) {
        Date parsed = parseDatabaseDate(value);
        return parsed != null && !parsed.after(today);
    }

    public static String formatBirthDateForDisplay(String value) {
        Date parsed = parseDatabaseDate(value);
        if (parsed == null) {
            return value;
        }
        return formatter("dd/MM/yyyy").format(parsed);
    }

    private static Date parseDatabaseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return formatter("yyyy-MM-dd").parse(value.trim());
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static SimpleDateFormat formatter(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setLenient(false);
        return format;
    }
}
