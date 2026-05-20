package com.example.fooddelivery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodDetailViewModel extends ViewModel {

    private final MutableLiveData<FoodItem> _foodItem    = new MutableLiveData<>();
    private final MutableLiveData<Boolean>  _isLoading   = new MutableLiveData<>(false);
    private final MutableLiveData<String>   _errorMsg    = new MutableLiveData<>();
    private final MutableLiveData<String>   _cartMessage = new MutableLiveData<>();

    public LiveData<FoodItem> getFoodItem()    { return _foodItem; }
    public LiveData<Boolean>  isLoading()      { return _isLoading; }
    public LiveData<String>   getErrorMsg()    { return _errorMsg; }
    public LiveData<String>   getCartMessage() { return _cartMessage; }

    /** Load chi tiết món ăn theo id — TODO: thay bằng API thật */
    public void loadFoodDetail(long foodId) {
        _isLoading.setValue(true);

        // Mock data — thay bằng repository.getFoodDetail(foodId)
        FoodItem item = new FoodItem(
                (int) foodId,
                "Bún thập cẩm",
                "Sợi bún tươi, tôm sông, gà đôi, nước dùng đậm đà",
                14, 35000,
                "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg"
        );
        _foodItem.setValue(item);
        _isLoading.setValue(false);
    }

    /** Thêm vào giỏ hàng — TODO: gọi CartRepository */
    public void addToCart(String bearerToken, long foodId, int quantity) {
        _cartMessage.setValue("Đã thêm " + quantity + " món vào giỏ hàng!");
    }
}