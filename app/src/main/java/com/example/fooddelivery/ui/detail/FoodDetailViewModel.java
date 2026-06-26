package com.example.fooddelivery.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.OrderRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<FoodItem> foodItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);
    private final OrderRepository orderRepository;

    public FoodDetailViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<FoodItem> getFoodItem() {
        return foodItem;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
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

    public void loadFoodDetail(long foodId) {
        isLoading.setValue(true);

        FoodItem item = new FoodItem(
                foodId,
                "Bun thap cam",
                "Bun tuoi, tom song, ga doi, nuoc dung dam da",
                14,
                35000,
                "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg"
        );
        foodItem.setValue(item);
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
