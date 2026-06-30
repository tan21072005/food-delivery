package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.RestaurantInfo;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import retrofit2.Call;

public class RestaurantRepository {
    private static final String RESTAURANT_INFO_SELECT =
            "id,name,description,address,logo_url,cover_url,avg_rating,total_reviews,is_open,status";

    private final ApiService apiService;

    public RestaurantRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<List<RestaurantInfo>> getRestaurantInfo(long restaurantId) {
        return apiService.getRestaurantInfoById(
                "eq." + restaurantId,
                RESTAURANT_INFO_SELECT,
                "is.null"
        );
    }
}
