package com.example.fooddelivery;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fooddelivery.OrderListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrderManagementActivity extends AppCompatActivity {

    // Tab TextViews
    private TextView tabPending, tabCompleted, tabCancelled;

    // Indicator Views (dải cam bên dưới tab)
    private View indicatorPending, indicatorCompleted, indicatorCancelled;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    // Màu
    private static final int COLOR_ACTIVE   = 0xFFFF6B35; // cam
    private static final int COLOR_INACTIVE = 0xFF999999; // xám
    private static final int COLOR_TRANSPARENT = 0x00000000;
    private static final int COLOR_INDICATOR   = 0xFFFF6B35;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_management);

        initViews();
        setupViewPager();
        setupTabClicks();
        setupBottomNav();

        // Mặc định hiển thị tab đầu tiên
        selectTab(0);
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tabPending   = findViewById(R.id.tabPending);
        tabCompleted = findViewById(R.id.tabCompleted);
        tabCancelled = findViewById(R.id.tabCancelled);

        indicatorPending   = findViewById(R.id.indicatorPending);
        indicatorCompleted = findViewById(R.id.indicatorCompleted);
        indicatorCancelled = findViewById(R.id.indicatorCancelled);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    // ── ViewPager2 ────────────────────────────────────────────────────────────

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return OrderListFragment.newInstance("pending");
                    case 1: return OrderListFragment.newInstance("completed");
                    case 2: return OrderListFragment.newInstance("cancelled");
                    default: return OrderListFragment.newInstance("pending");
                }
            }
            @Override public int getItemCount() { return 3; }
        });

        // Khi swipe → cập nhật tab UI
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }
        });

        // Tắt swipe nếu muốn chỉ dùng click (bỏ dòng này nếu muốn swipe)
        // viewPager.setUserInputEnabled(false);
    }

    // ── Tab click ─────────────────────────────────────────────────────────────

    private void setupTabClicks() {
        tabPending.setOnClickListener(v -> {
            viewPager.setCurrentItem(0, true);
            selectTab(0);
        });
        tabCompleted.setOnClickListener(v -> {
            viewPager.setCurrentItem(1, true);
            selectTab(1);
        });
        tabCancelled.setOnClickListener(v -> {
            viewPager.setCurrentItem(2, true);
            selectTab(2);
        });
    }

    /**
     * Cập nhật style cho 3 tab + 3 indicator theo tab đang active.
     * Gọi khi click tab hoặc khi swipe ViewPager2.
     */
    private void selectTab(int index) {
        // Reset tất cả về inactive
        tabPending.setTextColor(COLOR_INACTIVE);
        tabCompleted.setTextColor(COLOR_INACTIVE);
        tabCancelled.setTextColor(COLOR_INACTIVE);

        indicatorPending.setBackgroundColor(COLOR_TRANSPARENT);
        indicatorCompleted.setBackgroundColor(COLOR_TRANSPARENT);
        indicatorCancelled.setBackgroundColor(COLOR_TRANSPARENT);

        // Kích hoạt tab được chọn
        switch (index) {
            case 0:
                tabPending.setTextColor(COLOR_ACTIVE);
                indicatorPending.setBackgroundColor(COLOR_INDICATOR);
                break;
            case 1:
                tabCompleted.setTextColor(COLOR_ACTIVE);
                indicatorCompleted.setBackgroundColor(COLOR_INDICATOR);
                break;
            case 2:
                tabCancelled.setTextColor(COLOR_ACTIVE);
                indicatorCancelled.setBackgroundColor(COLOR_INDICATOR);
                break;
        }
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)      { /* navigate to Home */      return true; }
            if (id == R.id.nav_orders)    { return true; }
            if (id == R.id.nav_favorites) { /* navigate to Favorites */ return true; }
            if (id == R.id.nav_profile)   { /* navigate to Profile */   return true; }
            return false;
        });
    }
}
