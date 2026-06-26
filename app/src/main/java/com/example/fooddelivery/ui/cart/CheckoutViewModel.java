package com.example.fooddelivery.ui.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.CartRequest;
import com.example.fooddelivery.data.model.CartSummaryResponse;
import com.example.fooddelivery.data.model.CheckoutRequest;
import com.example.fooddelivery.data.repository.OrderRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<CartSummaryResponse> _cartSummary = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMsg = new MutableLiveData<>();
    private final MutableLiveData<List<Long>> _orderSuccess = new MutableLiveData<>();

    private final OrderRepository orderRepository;

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<CartSummaryResponse> getCartSummary() { return _cartSummary; }
    public LiveData<String> getErrorMsg() { return _errorMsg; }
    public LiveData<List<Long>> getOrderSuccess() { return _orderSuccess; }

    public void loadCartSummary() {
        _isLoading.setValue(true);
        orderRepository.getCartSummary().enqueue(new Callback<CartSummaryResponse>() {
            @Override
            public void onResponse(Call<CartSummaryResponse> call, Response<CartSummaryResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _cartSummary.setValue(response.body());
                } else {
                    _errorMsg.setValue("Lỗi tải giỏ hàng: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartSummaryResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMsg.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void updateQuantity(String cartId, long userId, long menuId, int newQuantity) {
        if (newQuantity <= 0) {
            deleteItem(cartId);
            return;
        }
        
        _isLoading.setValue(true);
        CartRequest request = new CartRequest(userId, menuId, newQuantity);
        orderRepository.updateCartQuantity("eq." + cartId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCartSummary(); // Tải lại sau khi cập nhật thành công
                } else {
                    _isLoading.setValue(false);
                    _errorMsg.setValue("Lỗi cập nhật: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMsg.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void deleteItem(String cartId) {
        _isLoading.setValue(true);
        orderRepository.removeFromCart("eq." + cartId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCartSummary();
                } else {
                    _isLoading.setValue(false);
                    _errorMsg.setValue("Lỗi xóa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMsg.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void checkout(String address, String note) {
        _isLoading.setValue(true);
        CheckoutRequest request = new CheckoutRequest(address, note);
        orderRepository.checkoutCart(request).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _orderSuccess.setValue(response.body()); // Trả về ID đơn hàng
                } else {
                    _errorMsg.setValue("Lỗi thanh toán: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMsg.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
