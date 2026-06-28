package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.CartRequest;
import com.example.fooddelivery.data.model.CartQuantityRequest;
import com.example.fooddelivery.data.model.CartSummaryResponse;
import com.example.fooddelivery.data.model.CheckoutRequest;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import retrofit2.Call;

public class OrderRepository {
    private final ApiService apiService;

    public OrderRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<CartSummaryResponse> getCartSummary() {
        return apiService.getCartSummary();
    }

    public Call<List<Long>> checkoutCart(CheckoutRequest request) {
        return apiService.checkoutCart(request);
    }

    public Call<Void> addToCart(long userId, long menuId, int quantity) {
        return apiService.addToCart(new CartRequest(userId, menuId, quantity));
    }

    public Call<Void> updateCartQuantity(String eqId, CartQuantityRequest request) {
        return apiService.updateCartQuantity(eqId, request);
    }

    public Call<Void> removeFromCart(String eqId) {
        return apiService.removeFromCart(eqId);
    }
}
