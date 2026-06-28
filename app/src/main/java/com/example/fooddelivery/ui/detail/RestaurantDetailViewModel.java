package com.example.fooddelivery.ui.detail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.FoodRepository;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailViewModel extends AndroidViewModel {

    private final FoodRepository foodRepository;
    private final MutableLiveData<List<FoodItem>> foodsLiveData = new MutableLiveData<>();

    public RestaurantDetailViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
    }

    public LiveData<List<FoodItem>> getFoods() {
        return foodsLiveData;
    }

    public void loadRestaurantFoods(long restaurantId) {
        // Fetch all foods or fetch by restaurantId.
        foodRepository.getMenus("*, restaurant:restaurants(*)").enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                // handle error
                foodsLiveData.setValue(new ArrayList<>());
            }
        });
    }
}
