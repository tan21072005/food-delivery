package com.example.fooddelivery.ui.menu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.FoodRepository;
import com.example.fooddelivery.data.repository.OrderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<FoodItem>> foodItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);
    private final MutableLiveData<Long> addedCartId = new MutableLiveData<>(-1L);
    private final MutableLiveData<Long> addedRestaurantId = new MutableLiveData<>(-1L);

    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;

    public MenuViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<List<FoodItem>> getFoodItems() { return foodItems; }
    public LiveData<String> getErrorMsg() { return errorMsg; }
    public LiveData<String> getCartMessage() { return cartMessage; }
    public LiveData<Boolean> getCartAddedEvent() { return cartAddedEvent; }
    public LiveData<Long> getAddedCartId() { return addedCartId; }
    public LiveData<Long> getAddedRestaurantId() { return addedRestaurantId; }

    public void consumeCartAddedEvent() {
        cartAddedEvent.setValue(false);
    }

    public void addToCart(int userId, long foodId, int quantity) {
        List<FoodItem> current = foodItems.getValue();
        if (current == null) {
            cartMessage.setValue("Khong tim thay mon");
            return;
        }
        for (FoodItem item : current) {
            if (item.getId() == foodId) {
                orderRepository.addToCartV3(item.getId(), quantity, null, Collections.emptyList())
                        .enqueue(new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {
                                if (!response.isSuccessful()) {
                                    cartMessage.setValue("Khong the them vao gio hang");
                                    return;
                                }
                                addedCartId.setValue(response.body() == null ? -1L : response.body());
                                addedRestaurantId.setValue(item.getRestaurantId());
                                cartMessage.setValue("Da them vao gio hang");
                                cartAddedEvent.setValue(true);
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                cartMessage.setValue("Khong the them vao gio hang: " + t.getMessage());
                            }
                        });
                return;
            }
        }
        cartMessage.setValue("Khong tim thay mon");
    }

    public void loadFoods(String categoryIdOrSlug) {
        loadMenu(categoryIdOrSlug, "", "id");
    }

    public void loadMenu(String categoryIdOrSlug, String keyword, String orderBy) {
        isLoading.setValue(true);
        errorMsg.setValue(null);

        buildMenuCall(categoryIdOrSlug).enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    foodItems.setValue(filterByKeyword(response.body(), keyword));
                } else {
                    foodItems.setValue(new ArrayList<>());
                    errorMsg.setValue("Khong tai duoc menu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                isLoading.setValue(false);
                foodItems.setValue(new ArrayList<>());
                errorMsg.setValue("Loi ket noi menu: " + t.getMessage());
            }
        });
    }

    public void loadRestaurantMenu(long restaurantId) {
        isLoading.setValue(true);
        errorMsg.setValue(null);
        if (restaurantId <= 0) {
            isLoading.setValue(false);
            foodItems.setValue(new ArrayList<>());
            errorMsg.setValue("Restaurant khong hop le");
            return;
        }

        foodRepository.getRestaurantMenu(restaurantId).enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                isLoading.setValue(false);
                foodItems.setValue(response.isSuccessful() && response.body() != null
                        ? response.body()
                        : new ArrayList<>());
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                isLoading.setValue(false);
                foodItems.setValue(new ArrayList<>());
                errorMsg.setValue("Loi ket noi menu: " + t.getMessage());
            }
        });
    }

    public void searchWithinLoadedMenu(String keyword) {
        List<FoodItem> current = foodItems.getValue();
        foodItems.setValue(filterByKeyword(current == null ? new ArrayList<>() : current, keyword));
    }

    private Call<List<FoodItem>> buildMenuCall(String categoryIdOrSlug) {
        if (categoryIdOrSlug == null || categoryIdOrSlug.trim().isEmpty()) {
            return foodRepository.getMenus(FoodRepository.MENU_SELECT);
        }
        try {
            return foodRepository.getMenusByDishCategory(Long.parseLong(categoryIdOrSlug.trim()));
        } catch (NumberFormatException ignored) {
            return foodRepository.getMenus(FoodRepository.MENU_SELECT);
        }
    }

    private List<FoodItem> filterByKeyword(List<FoodItem> items, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return items;
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem item : items) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
            String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase(Locale.ROOT);
            if (name.contains(normalizedKeyword) || description.contains(normalizedKeyword)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
