package com.example.fooddelivery.ui.menu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.FoodRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<FoodItem>> foodItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> cartMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddedEvent = new MutableLiveData<>(false);

    private final FoodRepository foodRepository;

    public MenuViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<List<FoodItem>> getFoodItems() {
        return foodItems;
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

    public void loadFoods(String categorySlug) {
        loadMenu(categorySlug, "", "sold_count");
    }

    public void loadMenu(String categorySlug, String keyword, String orderBy) {
        isLoading.setValue(true);
        Call<List<FoodItem>> call = (categorySlug == null || categorySlug.isEmpty())
                ? foodRepository.getMenus("*")
                : foodRepository.getMenusByCategory("eq." + categorySlug, "*");

        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                isLoading.setValue(false);
                List<FoodItem> items = response.isSuccessful() && response.body() != null
                        ? response.body()
                        : getMockFoods();
                foodItems.setValue(filterByKeyword(items, keyword));
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                isLoading.setValue(false);
                foodItems.setValue(filterByKeyword(getMockFoods(), keyword));
            }
        });
    }

    private List<FoodItem> filterByKeyword(List<FoodItem> items, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return items;
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem item : items) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
            if (name.contains(normalizedKeyword)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<FoodItem> getMockFoods() {
        FoodItem bunCha = new FoodItem(1, "Bun cha Ha Noi", "Bun cha thit nuong thom ngon", 120, 35000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/bun.png");
        bunCha.setRestaurantId(1);
        FoodItem phoBo = new FoodItem(2, "Pho bo tai nam", "Pho bo truyen thong", 200, 45000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/pho.png");
        phoBo.setRestaurantId(1);
        FoodItem comTam = new FoodItem(3, "Com tam suon bi", "Com tam Sai Gon", 150, 40000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/com.png");
        comTam.setRestaurantId(2);
        return Arrays.asList(bunCha, phoBo, comTam);
    }
}
