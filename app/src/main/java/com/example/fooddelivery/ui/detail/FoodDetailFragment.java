package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.databinding.FoodFragmentDetailBinding;
import com.example.fooddelivery.utils.MoneyFormatter;

public class FoodDetailFragment extends Fragment {

    private FoodFragmentDetailBinding binding;
    private FoodDetailViewModel viewModel;
    private long foodId;
    private int quantity = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FoodFragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FoodDetailViewModel.class);
        foodId = getArguments() != null ? getArguments().getLong("food_id", -1) : -1;

        setupListeners();
        observeViewModel();

        if (foodId != -1) {
            viewModel.loadFoodDetail(foodId);
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            FoodItem item = viewModel.getFoodItem().getValue();
            if (item != null) {
                addItemToCartWithRestaurantGuard(item, quantity);
            }
        });
    }

    private void addItemToCartWithRestaurantGuard(FoodItem item, int quantity) {
        addItemToCart(item, quantity);
    }

    private void addItemToCart(FoodItem item, int quantity) {
        LocalCart.getInstance().add(item, quantity);
        Toast.makeText(requireContext(), "Da them vao gio hang", Toast.LENGTH_SHORT).show();
    }

    private void observeViewModel() {
        viewModel.getFoodItem().observe(getViewLifecycleOwner(), item -> {
            if (item == null) return;
            binding.tvFoodName.setText(item.getName());
            binding.tvDescription.setText(item.getDescription());
            updateTotalPrice();

            Glide.with(requireContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .into(binding.imgFood);
        });

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalPrice() {
        FoodItem item = viewModel.getFoodItem().getValue();
        if (item == null) return;

        double total = item.getPrice() * quantity;
        binding.tvTotalPrice.setText("Tổng: " + MoneyFormatter.format(total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
