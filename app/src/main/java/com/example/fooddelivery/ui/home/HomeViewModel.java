package com.example.fooddelivery.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.HomeDataResponse;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    // ── LiveData ──
    private final MutableLiveData<Boolean>          _isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<List<FoodCategory>> _categories = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>>   _topSelling   = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>>   _allFoods     = new MutableLiveData<>();
    private final MutableLiveData<String>            _errorMsg     = new MutableLiveData<>();
    private final MutableLiveData<String>            _cartMessage  = new MutableLiveData<>();

    private final ApiService apiService;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        apiService = SupabaseClient.getInstance(application).create(ApiService.class);
    }

    // ── Expose as immutable LiveData ──
    public LiveData<Boolean>            isLoading()     { return _isLoading; }
    public LiveData<List<FoodCategory>> getCategories() { return _categories; }
    public LiveData<List<FoodItem>>     getTopSelling() { return _topSelling; }
    public LiveData<List<FoodItem>>     getAllFoods()    { return _allFoods; }
    public LiveData<String>             getErrorMsg()   { return _errorMsg; }
    public LiveData<String>             getCartMessage(){ return _cartMessage; }

    /**
     * Gọi khi Fragment khởi tạo hoặc pull-to-refresh.
     */
    public void loadHome() {
        _isLoading.setValue(true);

        apiService.getHomeData().enqueue(new Callback<HomeDataResponse>() {
            @Override
            public void onResponse(Call<HomeDataResponse> call, Response<HomeDataResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    HomeDataResponse data = response.body();
                    _categories.setValue(data.getCategories());
                    _topSelling.setValue(data.getTopSelling());
                    _allFoods.setValue(data.getAllFoods());
                } else {
                    _errorMsg.setValue("Lỗi tải dữ liệu: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<HomeDataResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMsg.setValue("Lỗi kết nối: " + t.getMessage());
                Log.e("HomeViewModel", "Error fetching home data", t);
            }
        });
    }

    /** Thêm vào giỏ hàng */
    public void addToCart(long userId, long foodId, int quantity) {
        com.example.fooddelivery.data.model.CartRequest request = new com.example.fooddelivery.data.model.CartRequest(userId, foodId, quantity);
        apiService.addToCart(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    _cartMessage.setValue("Đã thêm vào giỏ hàng!");
                } else {
                    _cartMessage.setValue("Lỗi khi thêm giỏ hàng: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _cartMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
