package com.example.fooddelivery.utils;

public class Constants {
    // Supabase Core
    public static final String SUPABASE_URL = com.example.fooddelivery.BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = com.example.fooddelivery.BuildConfig.SUPABASE_ANON_KEY;

    // Supabase Storage
    public static final String STORAGE_BASE_URL = SUPABASE_URL + "storage/v1/object/public/";
    public static final String BUCKET_FOOD_IMAGES = "food_images";
    public static final String BUCKET_RESTAURANT_LOGOS = "restaurant_logos";
}
