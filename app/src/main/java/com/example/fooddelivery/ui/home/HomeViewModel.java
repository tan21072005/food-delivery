package com.example.fooddelivery.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.HomeDataResponse;
import com.example.fooddelivery.data.repository.FoodRepository;
import com.example.fooddelivery.data.repository.LocationRepository;

import java.util.ArrayList;
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

    private final FoodRepository foodRepository;
    private final LocationRepository locationRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);
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

    public LiveData<android.location.Location> getCurrentLocation() {
        return locationRepository.getCurrentLocation();
    }

    public void loadHome() {
        isLoading.setValue(true);
        errorMsg.setValue(null);

        foodRepository.getHomeData().enqueue(new Callback<HomeDataResponse>() {
            @Override
            public void onResponse(Call<HomeDataResponse> call, Response<HomeDataResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    HomeDataResponse body = response.body();
                    List<FoodCategory> remoteCategories = body.getCategories();
                    List<FoodItem> remoteTopSelling = body.getTopSelling();
                    List<FoodItem> remoteAllFoods = body.getAllFoods();
                    if (remoteAllFoods == null) remoteAllFoods = remoteTopSelling;

                    categories.setValue(remoteCategories == null ? new ArrayList<>() : remoteCategories);
                    topSelling.setValue(remoteTopSelling == null ? new ArrayList<>() : remoteTopSelling);
                    allFoods.setValue(remoteAllFoods == null ? new ArrayList<>() : remoteAllFoods);
                } else {
                    loadHomeFromRestFallback("RPC trang chu loi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HomeDataResponse> call, Throwable t) {
                loadHomeFromRestFallback("RPC trang chu loi ket noi: " + t.getMessage());
            }
        });
    }

    private void loadHomeFromRestFallback(String rpcError) {
        foodRepository.getCategories("id,name,slug,icon_url").enqueue(new Callback<List<FoodCategory>>() {
            @Override
            public void onResponse(Call<List<FoodCategory>> call, Response<List<FoodCategory>> response) {
                categories.setValue(response.isSuccessful() && response.body() != null
                        ? response.body()
                        : new ArrayList<>());
                loadMenusFallback(rpcError);
            }

            @Override
            public void onFailure(Call<List<FoodCategory>> call, Throwable t) {
                categories.setValue(new ArrayList<>());
                loadMenusFallback(rpcError + "; cuisine loi: " + t.getMessage());
            }
        });
    }

    private void loadMenusFallback(String rpcError) {
        foodRepository.getMenus(FoodRepository.MENU_SELECT).enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    topSelling.setValue(response.body());
                    allFoods.setValue(response.body());
                    errorMsg.setValue(rpcError + ". Dang dung REST fallback.");
                } else {
                    topSelling.setValue(new ArrayList<>());
                    allFoods.setValue(new ArrayList<>());
                    errorMsg.setValue(rpcError + "; REST menu loi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                isLoading.setValue(false);
                topSelling.setValue(new ArrayList<>());
                allFoods.setValue(new ArrayList<>());
                errorMsg.setValue(rpcError + "; REST menu loi ket noi: " + t.getMessage());
            }
        });
    }

}
