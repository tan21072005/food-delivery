package com.example.fooddelivery.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;

import java.util.Arrays;
import java.util.List;

public class RestaurantDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<List<FoodItem>> foods = new MutableLiveData<>();

    public RestaurantDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<FoodItem>> getFoods() {
        return foods;
    }

    public void loadRestaurantFoods(long restaurantId) {
        FoodItem bunCha = new FoodItem(1, "Bun cha Ha Noi", "Bun cha thit nuong thom ngon", 120, 35000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/bun.png");
        bunCha.setRestaurantId(restaurantId);
        FoodItem phoBo = new FoodItem(2, "Pho bo tai nam", "Pho bo truyen thong", 200, 45000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/pho.png");
        phoBo.setRestaurantId(restaurantId);
        foods.setValue(Arrays.asList(bunCha, phoBo));
    }
}
