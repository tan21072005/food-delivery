package com.example.fooddelivery.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;

import java.util.Arrays;
import java.util.List;

public class HomeViewModel extends ViewModel {

    // ── LiveData ──
    private final MutableLiveData<Boolean>          _isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<List<FoodCategory>> _categories = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>>   _topSelling   = new MutableLiveData<>();
    private final MutableLiveData<List<FoodItem>>   _allFoods     = new MutableLiveData<>();
    private final MutableLiveData<String>            _errorMsg     = new MutableLiveData<>();
    private final MutableLiveData<String>            _cartMessage  = new MutableLiveData<>();

    // ── Expose as immutable LiveData ──
    public LiveData<Boolean>            isLoading()     { return _isLoading; }
    public LiveData<List<FoodCategory>> getCategories() { return _categories; }
    public LiveData<List<FoodItem>>     getTopSelling() { return _topSelling; }
    public LiveData<List<FoodItem>>     getAllFoods()    { return _allFoods; }
    public LiveData<String>             getErrorMsg()   { return _errorMsg; }
    public LiveData<String>             getCartMessage(){ return _cartMessage; }

    /**
     * Gọi khi Fragment khởi tạo hoặc pull-to-refresh.
     * TODO: Thay bằng gọi Repository -> API thật.
     */
    public void loadHome() {
        _isLoading.setValue(true);

        // ── Dữ liệu mẫu — thay bằng API call ──
        loadSampleCategories();
        loadSampleFoods();

        _isLoading.setValue(false);
    }

    /** Thêm vào giỏ hàng. TODO: gọi CartRepository */
    public void addToCart(String bearerToken, long foodId, int quantity) {
        // TODO: cartRepository.addToCart(bearerToken, foodId, quantity)
        //       .observe(owner, result -> _cartMessage.setValue(...))
        _cartMessage.setValue("Đã thêm vào giỏ hàng!");
    }

    // ─────────────────────────────────────────────────────────
    // Sample data (xoá khi có API thật)
    // ─────────────────────────────────────────────────────────
    private void loadSampleCategories() {
        List<FoodCategory> cats = Arrays.asList(
                new FoodCategory("1", "Bún",          "bun",
                        "https://res.cloudinary.com/daakugdmw/image/upload/ic_bun.png"),
                new FoodCategory("2", "Đồ ăn nhanh",  "do-an-nhanh",
                        "https://res.cloudinary.com/daakugdmw/image/upload/ic_fastfood.png"),
                new FoodCategory("3", "Nước uống",    "nuoc-uong",
                        "https://res.cloudinary.com/daakugdmw/image/upload/ic_drink.png"),
                new FoodCategory("4", "Cơm",          "com",
                        "https://res.cloudinary.com/daakugdmw/image/upload/ic_rice.png"),
                new FoodCategory("5", "Phở",          "pho",
                        "https://res.cloudinary.com/daakugdmw/image/upload/ic_pho.png")
        );
        _categories.setValue(cats);
    }

    //  đang gọi 7 tham số  nên sai
//    private void loadSampleFoods() {
//        List<FoodItem> foods = Arrays.asList(
//                new FoodItem(1, "Bún thập cẩm",
//                        "Sợi bún tươi, tôm sông, gà đôi", 35000, 14,
//                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg", "bun"),
//                new FoodItem(2, "Bún riêu cua",
//                        "Sợi bún tươi, cua đồng, cà chua", 35000, 145,
//                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_rieu_cua.jpg", "bun"),
//                new FoodItem(3, "Bún bò Huế",
//                        "Bún tươi, bò, chả, sả thơm", 40000, 144,
//                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_bo_hue.jpg", "bun"),
//                new FoodItem(4, "Bún giò heo",
//                        "Bún tươi, giò heo hầm mềm", 40000, 344,
//                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_gio_heo.jpg", "bun")
//        );
//        _topSelling.setValue(foods);
//        _allFoods.setValue(foods);
//    }

    private void loadSampleFoods() {
        List<FoodItem> foods = Arrays.asList(
                new FoodItem(1, "Bún thập cẩm",
                        "Sợi bún tươi, tôm sông, gà đôi", 14, 35000,
                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_thap_cam.jpg"),

                new FoodItem(2, "Bún riêu cua",
                        "Sợi bún tươi, cua đồng, cà chua", 145, 35000,
                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_rieu_cua.jpg"),

                new FoodItem(3, "Bún bò Huế",
                        "Bún tươi, bò, chả, sả thơm", 144, 40000,
                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_bo_hue.jpg"),

                new FoodItem(4, "Bún giò heo",
                        "Bún tươi, giò heo hầm mềm", 344, 40000,
                        "https://res.cloudinary.com/daakugdmw/image/upload/food_bun_gio_heo.jpg")
        );
        _topSelling.setValue(foods);
        _allFoods.setValue(foods);
    }

}
