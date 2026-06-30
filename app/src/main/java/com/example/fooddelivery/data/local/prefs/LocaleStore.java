package com.example.fooddelivery.data.local.prefs;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LocaleStore {
    private LocaleStore() {
    }

    public static String normalize(String languageTag) {
        return "en".equals(languageTag) ? "en" : "vi";
    }

    public static void apply(String languageTag) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(normalize(languageTag))
        );
    }
}
