package com.example.fooddelivery;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private NavController    navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Káº¿t ná»‘i NavController vá»›i BottomNavigationView
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        // 2. Láº¥y NavController tá»« NavHostFragment
        navController = navHost.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

            
        // áº¨n bottom nav á»Ÿ mÃ n hÃ¬nh login/register
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

