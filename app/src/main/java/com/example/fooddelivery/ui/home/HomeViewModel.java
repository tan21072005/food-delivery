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
import com.example.fooddelivery.data.repository.FoodRepository;
import com.example.fooddelivery.data.repository.LocationRepository;
import com.example.fooddelivery.data.repository.OrderRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<FoodCategory>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>> topSelling = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>> allFoods = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);

    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
        orderRepository = new OrderRepository(application);
        locationRepository = new LocationRepository(application);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<List<FoodCategory>> getCategories() {
        return categories;
    }

    public LiveData<List<FoodItem>> getTopSelling() {
        return topSelling;
    }

    public LiveData<List<FoodItem>> getAllFoods() {
        return allFoods;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public LiveData<String> getCartMessage() {
        return cartMessage;
    }

    public LiveData<Boolean> getCartAddedEvent() {
        return cartAddedEvent;
    }

    public void consumeCartAddedEvent() {
        cartAddedEvent.setValue(false);
    }

    public LiveData<android.location.Location> getCurrentLocation() {
        return locationRepository.getCurrentLocation();
    }

    public void loadHome() {
        isLoading.setValue(true);

        foodRepository.getHomeData().enqueue(new Callback<HomeDataResponse>() {
            @Override
            public void onResponse(Call<HomeDataResponse> call, Response<HomeDataResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    HomeDataResponse data = response.body();
                    categories.setValue(data.getCategories());
                    topSelling.setValue(data.getTopSelling());
                    allFoods.setValue(data.getAllFoods());
                } else {
                    errorMsg.setValue("Loi tai du lieu: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<HomeDataResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMsg.setValue("Loi ket noi: " + t.getMessage());
                Log.e("HomeViewModel", "Error fetching home data", t);
            }
        });
    }

    public void addToCart(long userId, long foodId, int quantity) {
        if (userId <= 0) {
            cartMessage.setValue("Vui long dang nhap lai de them vao gio");
            return;
        }

        orderRepository.addToCart(userId, foodId, quantity).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartMessage.setValue("Da them vao gio hang!");
                    cartAddedEvent.setValue(true);
                } else {
                    cartMessage.setValue("Loi khi them gio hang: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cartMessage.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }
}
