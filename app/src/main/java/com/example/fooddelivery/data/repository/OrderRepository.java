package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.AddToCartV3Request;
import com.example.fooddelivery.data.model.CartRequest;
import com.example.fooddelivery.data.model.CartSummaryResponse;
import com.example.fooddelivery.data.model.CartSummaryV3Request;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.CheckoutCartV3Request;
import com.example.fooddelivery.data.model.CheckoutRequest;
import com.example.fooddelivery.data.model.ClearCartV3Request;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.data.model.GetMyOrdersV3Request;
import com.example.fooddelivery.data.model.MyOrderV3Response;
import com.example.fooddelivery.data.model.RemoveCartItemV3Request;
import com.example.fooddelivery.data.model.UpdateCartItemQuantityV3Request;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;

public class OrderRepository {
    private final ApiService apiService;

    public OrderRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<Long> addToCartV3(long menuItemId, int quantity, String note, List<Long> optionChoiceIds) {
        List<Long> safeOptionChoiceIds = optionChoiceIds == null ? Collections.emptyList() : optionChoiceIds;
        return apiService.addToCartV3(new AddToCartV3Request(menuItemId, quantity, note, safeOptionChoiceIds));
    }

    public Call<List<DraftCartV3Response>> getDraftCartsV3() {
        return apiService.getDraftCartsV3(Collections.emptyMap());
    }

    public Call<CartSummaryV3Response> getCartSummaryV3(long cartId) {
        return apiService.getCartSummaryV3(new CartSummaryV3Request(cartId));
    }

    public Call<Long> checkoutCartV3(long cartId, long deliveryAddressId, String paymentMethod, String note) {
        return apiService.checkoutCartV3(new CheckoutCartV3Request(cartId, deliveryAddressId, paymentMethod, note));
    }

    public Call<Long> updateCartItemQuantityV3(long cartItemId, int quantity) {
        return apiService.updateCartItemQuantityV3(new UpdateCartItemQuantityV3Request(cartItemId, quantity));
    }

    public Call<Long> removeCartItemV3(long cartItemId) {
        return apiService.removeCartItemV3(new RemoveCartItemV3Request(cartItemId));
    }

    public Call<Long> clearCartV3(long cartId) {
        return apiService.clearCartV3(new ClearCartV3Request(cartId));
    }

    public Call<List<MyOrderV3Response>> getMyOrdersV3(String status) {
        return apiService.getMyOrdersV3(new GetMyOrdersV3Request(status));
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

    public Call<Void> updateCartQuantity(String eqId, CartRequest request) {
        return apiService.updateCartQuantity(eqId, request);
    }

    public Call<Void> removeFromCart(String eqId) {
        return apiService.removeFromCart(eqId);
    }
}
