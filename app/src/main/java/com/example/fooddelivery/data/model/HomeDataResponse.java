package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeDataResponse {
    
    @SerializedName("categories")
    private List<FoodCategory> categories;

    @SerializedName("cuisines")
    private List<FoodCategory> cuisines;
    
    @SerializedName("top_selling")
    private List<FoodItem> topSelling;
    
    @SerializedName("all_foods")
    private List<FoodItem> allFoods;

    public HomeDataResponse() {}

    public List<FoodCategory> getCategories() {
        return categories != null ? categories : cuisines;
    }

    public void setCategories(List<FoodCategory> categories) {
        this.categories = categories;
    }

    public List<FoodCategory> getCuisines() {
        return cuisines;
    }

    public void setCuisines(List<FoodCategory> cuisines) {
        this.cuisines = cuisines;
    }

    public List<FoodItem> getTopSelling() {
        return topSelling;
    }

    public void setTopSelling(List<FoodItem> topSelling) {
        this.topSelling = topSelling;
    }

    public List<FoodItem> getAllFoods() {
        return allFoods;
    }

    public void setAllFoods(List<FoodItem> allFoods) {
        this.allFoods = allFoods;
    }
}
