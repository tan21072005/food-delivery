package com.example.fooddelivery.data.local.prefs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocaleStoreTest {
    @Test
    public void missingLanguageDefaultsToVietnamese() {
        assertEquals("vi", LocaleStore.normalize(null));
    }

    @Test
    public void unsupportedLanguageDefaultsToVietnamese() {
        assertEquals("vi", LocaleStore.normalize("fr"));
    }

    @Test
    public void englishIsSupported() {
        assertEquals("en", LocaleStore.normalize("en"));
    }

    @Test
    public void vietnameseIsSupported() {
        assertEquals("vi", LocaleStore.normalize("vi"));
    }
}
