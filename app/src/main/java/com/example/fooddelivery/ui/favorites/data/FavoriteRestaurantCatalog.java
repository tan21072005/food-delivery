package com.example.fooddelivery.ui.favorites.data;

import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.favorites.model.FavoriteRestaurant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FavoriteRestaurantCatalog {
    private static final List<FavoriteRestaurant> RESTAURANTS = Collections.unmodifiableList(Arrays.asList(
            new FavoriteRestaurant("guu_chicken", "Guu Chicken - Cơm Gà, Gà Rán & Mỳ Ý", "4.5 (28)", "800+", "0.3 km", R.drawable.banner_food),
            new FavoriteRestaurant("lotteria", "LOTTERIA - PHẠM NGỌC THẠCH", "4.6 (206)", "41K+", "2.8 km", R.drawable.food_bun_bo_hue),
            new FavoriteRestaurant("kfc", "KFC - Lê Thanh Nghị", "4.6 (122)", "2.7K+", "0.8 km", R.drawable.food_bun_gio_heo),
            new FavoriteRestaurant("mokchang", "Mokchang - Đồ Ăn & Gà Rán Hàn Quốc", "4.7 (29)", "700+", "1.4 km", R.drawable.food_bun_rieu_cua),
            new FavoriteRestaurant("sau_sun", "Sâu Sùn Coffee & Tea", "4.8 (91)", "1K+", "1.9 km", R.drawable.food_bun_thap_cam)
    ));

    private FavoriteRestaurantCatalog() {}

    public static List<FavoriteRestaurant> all() { return RESTAURANTS; }

    public static FavoriteRestaurant findById(String id) {
        for (FavoriteRestaurant restaurant : RESTAURANTS) {
            if (restaurant.getId().equals(id)) return restaurant;
        }
        return null;
    }
}
