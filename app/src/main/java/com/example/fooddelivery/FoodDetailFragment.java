package com.example.fooddelivery;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.FragmentFoodDetailBinding;
import com.example.fooddelivery.SessionManager;
import com.example.fooddelivery.FoodDetailViewModel;

public class FoodDetailFragment extends Fragment {

    private FragmentFoodDetailBinding binding;
    private FoodDetailViewModel viewModel;
    private SessionManager session;
    private long foodId;
    private int quantity = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(FoodDetailViewModel.class);

        // Nhận food_id từ HomeFragment hoặc MenuFragment
        if (getArguments() != null) {
            foodId = getArguments().getLong("food_id", -1);
        }

        setupListeners();
        observeViewModel();

        if (foodId != -1) viewModel.loadFoodDetail(foodId);
    }

    private void setupListeners() {
        // Nút back
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        // Tăng số lượng
        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        // Giảm số lượng
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

//        // Thêm vào giỏ hàng
//        binding.btnAddToCart.setOnClickListener(v -> {
//            if (!session.isLoggedIn()) {
//                Toast.makeText(requireContext(),
//                        "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_foodDetail_to_login);
//                return;
//            }
//            viewModel.addToCart(session.getBearerToken(), foodId, quantity);
//        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
        );

        viewModel.getFoodItem().observe(getViewLifecycleOwner(), item -> {
            if (item == null) return;
            binding.tvFoodName.setText(item.getName());
            binding.tvDescription.setText(item.getDescription());
//            binding.tvPrice.setText(item.getPriceFormatted());
//            binding.tvSoldCount.setText(item.getSoldCountLabel());
//            binding.tvCategory.setText(item.getCategoryName());
            updateTotalPrice();

            Glide.with(requireContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .into(binding.imgFood);
        });

        viewModel.getCartMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTotalPrice() {
        if (viewModel.getFoodItem().getValue() == null) return;
        double total = viewModel.getFoodItem().getValue().getPrice() * quantity;
        long rounded = Math.round(total);
        String formatted = String.format("%,d", rounded).replace(",", ".") + "đ";
        binding.tvTotalPrice.setText("Tổng: " + formatted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}