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

    private final MutableLiveData<List<FoodItem>> foods = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final FoodRepository foodRepository;

    public RestaurantDetailViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
    }

    public LiveData<List<FoodItem>> getFoods() {
        return foods;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public void loadRestaurantFoods(long restaurantId) {
        errorMsg.setValue(null);
        if (restaurantId <= 0) {
            foods.setValue(new ArrayList<>());
            errorMsg.setValue("Restaurant khong hop le");
            return;
        }

        foodRepository.getRestaurantMenu(restaurantId).enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foods.setValue(response.body());
                } else {
                    foods.setValue(new ArrayList<>());
                    errorMsg.setValue("Khong tai duoc menu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                foods.setValue(new ArrayList<>());
                errorMsg.setValue("Loi ket noi menu: " + t.getMessage());
            }
        });
    }
}
