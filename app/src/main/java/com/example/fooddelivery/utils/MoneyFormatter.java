package com.example.fooddelivery.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        FORMATTER = new DecimalFormat("#,###", symbols);
    }

    private MoneyFormatter() {}

    public static String format(long amount) {
        return FORMATTER.format(amount) + "đ";
    }

    public static String format(double amount) {
        return format(Math.round(amount));
    }
}
