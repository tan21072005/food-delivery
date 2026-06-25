package com.example.fooddelivery.ui.menu;

import com.example.fooddelivery.data.model.FoodItem;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MenuViewModel extends ViewModel {

    private final MutableLiveData<List<FoodItem>> _foodItems  = new MutableLiveData<>();
    private final MutableLiveData<Boolean>         _isLoading  = new MutableLiveData<>(false);
    private final MutableLiveData<String>          _errorMsg   = new MutableLiveData<>();
    private final MutableLiveData<String>          _cartMessage= new MutableLiveData<>();

    // Cache toàn bộ danh sách để search/filter local
    private final List<FoodItem> allFoods = Arrays.asList(
            new FoodItem(1, "Bún thập cẩm",  "Sợi bún tươi, tôm sông, gà đôi",  14,  35000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg"),
            new FoodItem(2, "Bún riêu cua",   "Sợi bún tươi, cua đồng, cà chua", 145, 35000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_rieu_cua.jpg"),
            new FoodItem(3, "Bún bò Huế",     "Bún tươi, bò, chả, sả thơm",      144, 40000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_bo_hue.jpg"),
            new FoodItem(4, "Burger bò",      "Beef patty, phô mai, rau tươi",    88,  59000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_burger.jpg"),
            new FoodItem(5, "Gà rán giòn",    "Gà rán giòn tan, ướp 24 tiếng",   210, 49000,
                    "https://res.cloudinary.com/daakugdmw/image/upload/food_ga_ran.jpg")
    );

    // ── Expose LiveData (read-only) ──
    public LiveData<List<FoodItem>> getFoodItems()    { return _foodItems; }  // ← đúng tên SearchFragment gọi
    public LiveData<Boolean>         isLoading()      { return _isLoading; }
    public LiveData<String>          getErrorMsg()    { return _errorMsg; }
    public LiveData<String>          getCartMessage() { return _cartMessage; }

    /**
     * Load danh sách món — dùng cho cả MenuFragment và SearchFragment
     * @param categorySlug  "" = tất cả, "bun" = lọc theo danh mục
     * @param keyword       "" = không tìm kiếm, "phở" = tìm theo tên
     * @param sortBy        "sold_count" | "price" | "name"
     */
    public void loadMenu(String categorySlug, String keyword, String sortBy) {
        _isLoading.setValue(true);

        new Thread(() -> {
            try {
                Thread.sleep(300); // giả lập network delay

                List<FoodItem> result = allFoods.stream()
                        // Lọc theo category (nếu có)
                        .filter(f -> categorySlug == null || categorySlug.isEmpty()
                                || f.getName().toLowerCase().contains(categorySlug.toLowerCase()))
                        // Lọc theo keyword (nếu có)
                        .filter(f -> keyword == null || keyword.isEmpty()
                                || f.getName().toLowerCase().contains(keyword.toLowerCase())
                                || f.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                        // Sắp xếp
                        .sorted((a, b) -> {
                            if ("price".equals(sortBy))
                                return Double.compare(a.getPrice(), b.getPrice());
                            if ("name".equals(sortBy))
                                return a.getName().compareTo(b.getName());
                            return Integer.compare(b.getSoldCount(), a.getSoldCount()); // sold_count
                        })
                        .collect(Collectors.toList());

                _foodItems.postValue(result);
                _isLoading.postValue(false);

            } catch (InterruptedException e) {
                _errorMsg.postValue("Lỗi tải dữ liệu");
                _isLoading.postValue(false);
            }
        }).start();
    }

    /** Shortcut load không cần keyword */
    public void loadFoods(String categorySlug) {
        loadMenu(categorySlug, "", "sold_count");
    }

    /** Thêm vào giỏ */
    public void addToCart(String bearerToken, long foodId, int quantity) {
        _cartMessage.postValue("Đã thêm vào giỏ hàng!");
    }
}