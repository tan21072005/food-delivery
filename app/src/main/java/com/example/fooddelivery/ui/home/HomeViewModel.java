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

        // Mock Categories
        List<FoodCategory> mockCategories = java.util.Arrays.asList(
                new FoodCategory(1, "Bun", "Bún", "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/bun.png"),
                new FoodCategory(2, "Pho", "Phở", "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/pho.png"),
                new FoodCategory(3, "Com", "Cơm", "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/com.png"),
                new FoodCategory(4, "Nuoc", "Đồ Uống", "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/nuoc.png")
        );
        categories.setValue(mockCategories);

        // Mock Foods
        List<FoodItem> mockFoods = java.util.Arrays.asList(
                new FoodItem(1, "Bún chả Hà Nội", "Bún chả thịt nướng thơm ngon", 120, 35000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/bun.png"),
                new FoodItem(2, "Phở bò tái nạm", "Phở bò truyền thống", 200, 45000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/pho.png"),
                new FoodItem(3, "Cơm tấm sườn bì", "Cơm tấm Sài Gòn", 150, 40000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/com.png")
        );
        topSelling.setValue(mockFoods);
        allFoods.setValue(mockFoods);

        isLoading.setValue(false);
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
