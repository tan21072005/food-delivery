package com.example.fooddelivery;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
//import com.example.fooddelivery.adapter.MenuAdapter;
//import com.example.fooddelivery.model.FoodItem;
import com.example.fooddelivery.MenuAdapter;
import com.example.fooddelivery.FoodItem;


import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private ImageButton btnBack, btnFavorite;
    private ImageView imgBanner;
    private RecyclerView recyclerViewMenu;
    private MenuAdapter adapter;

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadBanner();
    }

    private void initViews() {
        btnBack       = findViewById(R.id.btnBack);
        btnFavorite   = findViewById(R.id.btnFavorite);
        imgBanner     = findViewById(R.id.imgBanner);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
    }

    private void setupRecyclerView() {
        List<FoodItem> items = getSampleData();
        adapter = new MenuAdapter(this, items);

        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setAdapter(adapter);
        recyclerViewMenu.setHasFixedSize(false);

        adapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onFoodClick(FoodItem item) {
                // TODO: Navigate to FoodDetailActivity
                // Intent intent = new Intent(MenuActivity.this, FoodDetailActivity.class);
                // intent.putExtra("food_id", item.getId());
                // startActivity(intent);
                Toast.makeText(MenuActivity.this,
                        "Xem chi tiết: " + item.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                // TODO: Add to cart via CartViewModel
                Toast.makeText(MenuActivity.this,
                        "Đã thêm: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            btnFavorite.setImageResource(
                    isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
            );
            Toast.makeText(this,
                    isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
                    Toast.LENGTH_SHORT).show();
        });

        // Search icon click — show an inline search bar or SearchView
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            // TODO: Toggle a search EditText above the list, or open SearchActivity
            Toast.makeText(this, "Tìm kiếm món ăn...", Toast.LENGTH_SHORT).show();
        });

        // Filter icon click
        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            // TODO: Show a BottomSheetDialogFragment with filter options
            Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show();
        });
    }

    /** Load banner image from URL or drawable */
    private void loadBanner() {
        // Replace with your actual URL from API/intent extras
        String bannerUrl = getIntent().getStringExtra("restaurant_banner_url");
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            Glide.with(this)
                    .load(bannerUrl)
                    .placeholder(R.drawable.banner_food)
                    .into(imgBanner);
        }
        // else the XML default drawable is shown
    }

    // ---------------------------------------------------------------
    // Sample data — replace with API call via ViewModel + Repository
    // ---------------------------------------------------------------
    private List<FoodItem> getSampleData() {
        List<FoodItem> list = new ArrayList<>();
        list.add(new FoodItem(1, "Bún thập cẩm",
                "Sợi bún tươi, tôm sông, gà đôi", 14, 35000, R.drawable.food_bun_thap_cam));
        list.add(new FoodItem(2, "Bún riêu cua",
                "Sợi bún tươi, tôm sông, gà đôi", 145, 35000, R.drawable.food_bun_rieu_cua));
        list.add(new FoodItem(3, "Bún bò Huế",
                "Sợi bún tươi, tôm sông, gà đôi", 144, 40000, R.drawable.food_bun_bo_hue));
        list.add(new FoodItem(4, "Bún giò heo",
                "Sợi bún tươi, tôm sông, gà đôi", 344, 40000, R.drawable.food_bun_gio_heo));

        list.add(new FoodItem(1, "Bún thập cẩm",
                "Sợi bún tươi, tôm sông, gà đôi", 14, 35000, R.drawable.food_bun_thap_cam));
        list.add(new FoodItem(2, "Bún riêu cua",
                "Sợi bún tươi, tôm sông, gà đôi", 145, 35000, R.drawable.food_bun_rieu_cua));
        list.add(new FoodItem(3, "Bún bò Huế",
                "Sợi bún tươi, tôm sông, gà đôi", 144, 40000, R.drawable.food_bun_bo_hue));
        list.add(new FoodItem(4, "Bún giò heo",
                "Sợi bún tươi, tôm sông, gà đôi", 344, 40000, R.drawable.food_bun_gio_heo));

        list.add(new FoodItem(1, "Bún thập cẩm",
                "Sợi bún tươi, tôm sông, gà đôi", 14, 35000, R.drawable.food_bun_thap_cam));
        list.add(new FoodItem(2, "Bún riêu cua",
                "Sợi bún tươi, tôm sông, gà đôi", 145, 35000, R.drawable.food_bun_rieu_cua));
        list.add(new FoodItem(3, "Bún bò Huế",
                "Sợi bún tươi, tôm sông, gà đôi", 144, 40000, R.drawable.food_bun_bo_hue));
        list.add(new FoodItem(4, "Bún giò heo",
                "Sợi bún tươi, tôm sông, gà đôi", 344, 40000, R.drawable.food_bun_gio_heo));
        return list;
    }
}