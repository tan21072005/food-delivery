package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.HomeDataResponse;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import retrofit2.Call;

public class FoodRepository {
    public static final String MENU_SELECT =
            "id,restaurant_id,category_id,item_name,description,image_url,price,rating,status,sold_count";

    private final ApiService apiService;

    public FoodRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<List<FoodCategory>> getCategories(String select) {
        return apiService.getCategories(select);
    }

    public Call<List<FoodItem>> getMenus(String select) {
        return apiService.getMenus(select);
    }

    public Call<List<FoodItem>> getMenusByCategory(String categoryId, String select) {
        return apiService.getMenusByCategory(categoryId, select);
    }

    public Call<List<FoodItem>> getMenusByDishCategory(long dishCategoryId) {
        return getMenusByCategory("eq." + dishCategoryId, MENU_SELECT);
    }

    public Call<List<FoodItem>> getRestaurantMenu(long restaurantId) {
        return apiService.getMenusByRestaurant("eq." + restaurantId, MENU_SELECT);
    }

    public Call<List<FoodItem>> getFoodById(long foodId) {
        return apiService.getMenuItemById("eq." + foodId, MENU_SELECT);
    }

    public Call<HomeDataResponse> getHomeData() {
        return apiService.getHomeData();
    }
}
