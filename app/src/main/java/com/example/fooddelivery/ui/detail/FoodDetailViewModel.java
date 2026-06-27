package com.example.fooddelivery.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;

public class FoodDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<FoodItem> foodItem = new MutableLiveData<>();
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();

    public FoodDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<FoodItem> getFoodItem() {
        return foodItem;
    }

    public LiveData<String> getCartMessage() {
        return cartMessage;
    }

    public LiveData<Boolean> getCartAddedEvent() {
        return cartAddedEvent;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    public void consumeCartAddedEvent() {
        cartAddedEvent.setValue(false);
    }

    public void loadFoodDetail(long foodId) {
        isLoading.setValue(true);
        FoodItem item = new FoodItem(foodId, "Mon an #" + foodId, "Mon ngon duoc chon tu menu", 100, 35000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/bun.png");
        item.setRestaurantId(1);
        foodItem.setValue(item);
        isLoading.setValue(false);
    }
}
