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

    public Call<HomeDataResponse> getHomeData() {
        return apiService.getHomeData();
    }
}
