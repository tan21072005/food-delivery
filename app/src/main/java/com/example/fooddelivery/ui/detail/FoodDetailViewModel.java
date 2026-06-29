package com.example.fooddelivery.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.FoodRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<FoodItem> foodItem = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final FoodRepository foodRepository;

    public FoodDetailViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<FoodItem> getFoodItem() {
        return foodItem;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public void loadFoodDetail(long foodId) {
        isLoading.setValue(true);
        errorMsg.setValue(null);

        if (foodId <= 0) {
            foodItem.setValue(null);
            errorMsg.setValue("Mon khong hop le");
            isLoading.setValue(false);
            return;
        }

        foodRepository.getFoodById(foodId).enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    foodItem.setValue(response.body().get(0));
                } else {
                    foodItem.setValue(null);
                    errorMsg.setValue("Khong tai duoc mon: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                isLoading.setValue(false);
                foodItem.setValue(null);
                errorMsg.setValue("Loi ket noi mon: " + t.getMessage());
            }
        });
    }
}
