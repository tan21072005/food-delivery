package com.example.fooddelivery.ui.menu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.OrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<List<FoodItem>> foodItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);
    private final OrderRepository orderRepository;

    private final List<FoodItem> allFoods = Arrays.asList(
            new FoodItem(1, "Bun thap cam", "Bun tuoi, tom song, ga doi", 14, 35000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg"),
            new FoodItem(2, "Bun rieu cua", "Bun tuoi, cua dong, ca chua", 145, 35000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_rieu_cua.jpg"),
            new FoodItem(3, "Bun bo Hue", "Bun tuoi, bo, cha, sa thom", 144, 40000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_bo_hue.jpg"),
            new FoodItem(4, "Burger bo", "Beef patty, pho mai, rau tuoi", 88, 59000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_burger.jpg"),
            new FoodItem(5, "Ga ran gion", "Ga ran gion tan, uop 24 tieng", 210, 49000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_ga_ran.jpg")
    );

    public MenuViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<List<FoodItem>> getFoodItems() {
        return foodItems;
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

    public void loadMenu(String categorySlug, String keyword, String sortBy) {
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                Thread.sleep(300);

                List<FoodItem> result = allFoods.stream()
                        .filter(food -> categorySlug == null || categorySlug.isEmpty()
                                || food.getName().toLowerCase().contains(categorySlug.toLowerCase()))
                        .filter(food -> keyword == null || keyword.isEmpty()
                                || food.getName().toLowerCase().contains(keyword.toLowerCase())
                                || food.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                        .sorted((left, right) -> {
                            if ("price".equals(sortBy)) {
                                return Double.compare(left.getPrice(), right.getPrice());
                            }
                            if ("name".equals(sortBy)) {
                                return left.getName().compareTo(right.getName());
                            }
                            return Integer.compare(right.getSoldCount(), left.getSoldCount());
                        })
                        .collect(Collectors.toList());

                foodItems.postValue(result);
                isLoading.postValue(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errorMsg.postValue("Loi tai du lieu");
                isLoading.postValue(false);
            }
        }).start();
    }

    public void loadFoods(String categorySlug) {
        loadMenu(categorySlug, "", "sold_count");
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
