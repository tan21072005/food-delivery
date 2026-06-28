package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.detail.adapters.StorefrontAdapter;
import com.google.android.material.appbar.AppBarLayout;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantDetailViewModel viewModel;
    private StorefrontAdapter adapter;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RestaurantDetailViewModel.class);

        toolbar = view.findViewById(R.id.toolbar);
        appBarLayout = view.findViewById(R.id.appBarLayout);

        // Handle back button
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Setup RecyclerView
        RecyclerView rvFoods = view.findViewById(R.id.rvFoods);
        rvFoods.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new StorefrontAdapter(requireContext());
        rvFoods.setAdapter(adapter);

        adapter.setOnItemClickListener(new StorefrontAdapter.OnItemClickListener() {
            @Override
            public void onFoodClick(com.example.fooddelivery.data.model.FoodItem item) {
                // Navigate to FoodDetailFragment
                Bundle bundle = new Bundle();
                bundle.putLong("food_id", item.getId());
                Navigation.findNavController(view).navigate(R.id.action_restaurantDetail_to_foodDetail, bundle);
            }

            @Override
            public void onAddToCartClick(com.example.fooddelivery.data.model.FoodItem item) {
                // CartManager.getInstance().addItem(item, 1);
                Toast.makeText(requireContext(), "Đã thêm " + item.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe data
        viewModel.getFoods().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
        });

        // Load dummy data for now
        viewModel.loadRestaurantFoods(1L);
    }
}
