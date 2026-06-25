package com.example.fooddelivery;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController    navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Kết nối NavController với BottomNavigationView
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        // 2. Lấy NavController từ NavHostFragment
        navController = navHost.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

            
        // Ẩn bottom nav ở màn hình login/register
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            int id = destination.getId();
//            if (id == R.id.loginFragment || id == R.id.registerFragment) {
            if (id == R.id.loginFragment) {
                binding.bottomNav.setVisibility(View.GONE);
            } else {
                binding.bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }
}
