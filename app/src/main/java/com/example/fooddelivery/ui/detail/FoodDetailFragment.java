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
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.databinding.FoodFragmentDetailBinding;
import com.example.fooddelivery.utils.MoneyFormatter;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailFragment extends Fragment {

    private FoodFragmentDetailBinding binding;
    private FoodDetailViewModel viewModel;
    private OrderRepository orderRepository;
    private long foodId;
    private int quantity = 1;
    private boolean isAdding = false;

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
        orderRepository = new OrderRepository(requireContext());
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
            if (item != null && !isAdding) {
                addItemToCart(item, quantity);
            }
        });
    }

    private void addItemToCart(FoodItem item, int quantity) {
        isAdding = true;
        binding.btnAddToCart.setEnabled(false);
        binding.btnAddToCart.setText("Dang them...");

        String note = binding.edNote.getText().toString();
        String safeNote = note == null || note.trim().isEmpty() ? null : note.trim();
        orderRepository.addToCartV3(item.getId(), quantity, safeNote, Collections.emptyList())
                .enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                        if (!isAdded()) return;
                        isAdding = false;
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Them vao gio");
                        if (response.isSuccessful()) {
                            Bundle result = new Bundle();
                            result.putLong("cart_id", response.body() == null ? -1L : response.body());
                            result.putLong("restaurant_id", item.getRestaurantId());
                            getParentFragmentManager().setFragmentResult("cart_changed", result);
                            Toast.makeText(requireContext(), "Da them vao gio hang", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigateUp();
                            return;
                        }
                        Toast.makeText(requireContext(), "Khong the them mon vao gio", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        isAdding = false;
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Them vao gio");
                        Toast.makeText(requireContext(),
                                "Khong the them mon vao gio: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        binding.tvTotalPrice.setText(MoneyFormatter.format(total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
