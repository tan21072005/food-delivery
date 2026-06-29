package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.fooddelivery.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private NavController    navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Kết nối NavController với BottomNavigationView
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        navController = navHost.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // Ẩn bottom nav ở màn hình login
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            int id = destination.getId();
            if (id == R.id.loginFragment
                    || id == R.id.accountMenuFragment
                    || id == R.id.accountInfoFragment
                    || id == R.id.collectionNameFragment
                    || id == R.id.collectionRestaurantsFragment) {
                binding.bottomNav.setVisibility(View.GONE);
                binding.navHostFragment.setPadding(0, 0, 0, 0);
            } else {
                binding.bottomNav.setVisibility(View.VISIBLE);
                binding.navHostFragment.setPadding(0, 0, 0,
                        (int) (56 * getResources().getDisplayMetrics().density));
            }
        });

        // Nếu được mở từ Checkout với yêu cầu chuyển sang tab Orders
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "orders".equals(intent.getStringExtra("open_tab"))) {
            // Chuyển bottom nav sang tab Đơn hàng
            binding.bottomNav.setSelectedItemId(R.id.nav_ordes);
        }
    }
}
