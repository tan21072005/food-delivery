package com.example.fooddelivery.ui.menu;

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
import com.example.fooddelivery.ui.menu.adapters.MenuAdapter;
import com.example.fooddelivery.data.model.FoodItem;


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
        setContentView(R.layout.menu_activity);

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
                        "Xem chi tiáº¿t: " + item.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                // TODO: Add to cart via CartViewModel
                Toast.makeText(MenuActivity.this,
                        "ÄÃ£ thÃªm: " + item.getName(), Toast.LENGTH_SHORT).show();
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
                    isFavorite ? "ÄÃ£ thÃªm vÃ o yÃªu thÃ­ch" : "ÄÃ£ bá» yÃªu thÃ­ch",
                    Toast.LENGTH_SHORT).show();
        });

        // Search icon click â€” show an inline search bar or SearchView
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            // TODO: Toggle a search EditText above the list, or open SearchActivity
            Toast.makeText(this, "TÃ¬m kiáº¿m mÃ³n Äƒn...", Toast.LENGTH_SHORT).show();
        });

        // Filter icon click
        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            // TODO: Show a BottomSheetDialogFragment with filter options
            Toast.makeText(this, "Bá»™ lá»c", Toast.LENGTH_SHORT).show();
        });
    }

    /** Load banner image from URL or drawable */
    private void loadBanner() {

        // URL áº£nh máº·c Ä‘á»‹nh tá»« Cloudinary
        String defaultBanner = "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/banner_food.jpg";
        // Replace with your actual URL from API/intent extras
        String bannerUrl = getIntent().getStringExtra("restaurant_banner_url");
        String urlToLoad = (bannerUrl == null || bannerUrl.isEmpty()) ? defaultBanner : bannerUrl;
            Glide.with(this)
                    .load(urlToLoad)
                    .placeholder(R.drawable.placeholder_food) // áº£nh chá» load
                    .error(R.drawable.placeholder_food)// áº£nh náº¿u lá»—i
                    .into(imgBanner);

    }

    // ---------------------------------------------------------------
    // Sample data â€” replace with API call via ViewModel + Repository
    // ---------------------------------------------------------------
    private List<FoodItem> getSampleData() {
        List<FoodItem> list = new ArrayList<>();
        list.add(new FoodItem(1, "BÃºn tháº­p cáº©m",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 14, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_thap_cam_dkoipu.jpg"));
        list.add(new FoodItem(2, "BÃºn riÃªu cua",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 145, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_rieu_cua_tqfob4.jpg"));
        list.add(new FoodItem(3, "BÃºn bÃ² Huáº¿",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 144, 40000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_bo_hue_mwuawo.jpg"));
        list.add(new FoodItem(4, "BÃºn giÃ² heo",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 344, 40000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_gio_heo_lcwlap.jpg"));


        list.add(new FoodItem(1, "BÃºn tháº­p cáº©m",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 14, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_thap_cam_dkoipu.jpg"));
        list.add(new FoodItem(2, "BÃºn riÃªu cua",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 145, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_rieu_cua_tqfob4.jpg"));
        list.add(new FoodItem(3, "BÃºn bÃ² Huáº¿",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 144, 40000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_bo_hue_mwuawo.jpg"));
        list.add(new FoodItem(4, "BÃºn giÃ² heo",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 344, 40000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_gio_heo_lcwlap.jpg"));


        list.add(new FoodItem(1, "BÃºn tháº­p cáº©m",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 14, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_thap_cam_dkoipu.jpg"));
        list.add(new FoodItem(2, "BÃºn riÃªu cua",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 145, 35000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_rieu_cua_tqfob4.jpg"));
        list.add(new FoodItem(3, "BÃºn bÃ² Huáº¿",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 144, 40000,"https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_bo_hue_mwuawo.jpg"));
        list.add(new FoodItem(4, "BÃºn giÃ² heo",
                "Sá»£i bÃºn tÆ°Æ¡i, tÃ´m sÃ´ng, gÃ  Ä‘Ã´i", 344, 40000, "https://res.cloudinary.com/daakugdmw/image/upload/v1778945025/food_bun_gio_heo_lcwlap.jpg"));


        return list;
    }
}
