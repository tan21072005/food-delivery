package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

        toolbar    = view.findViewById(R.id.toolbar);
        appBarLayout = view.findViewById(R.id.appBarLayout);

        // Back button
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // ── Click tên nhà hàng → Thông tin quán ──────────────────────────────
        View tvRestaurantName = view.findViewById(R.id.tvRestaurantName);
        if (tvRestaurantName != null) {
            tvRestaurantName.setOnClickListener(v ->
                    Navigation.findNavController(v)
                            .navigate(R.id.action_restaurantDetail_to_info));
        }

        // ── Click khu vực Ưu đãi → Khuyến mại ───────────────────────────────
        View promoSection = view.findViewById(R.id.promoSection);
        if (promoSection != null) {
            promoSection.setOnClickListener(v ->
                    Navigation.findNavController(v)
                            .navigate(R.id.action_restaurantDetail_to_promotions));
        }

        // ── Click "đánh giá" / số sao → Đánh giá của quán ───────────────────
        View layoutRatingReview = view.findViewById(R.id.layoutRatingReview);
        if (layoutRatingReview != null) {
            layoutRatingReview.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                if (getArguments() != null) {
                    bundle.putLong("restaurant_id", getArguments().getLong("restaurant_id", -1L));
                }
                Navigation.findNavController(v)
                        .navigate(R.id.action_restaurantDetail_to_reviews, bundle);
            });
        }

        // ── RecyclerView foods ────────────────────────────────────────────────
        RecyclerView rvFoods = view.findViewById(R.id.rvFoods);
        rvFoods.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new StorefrontAdapter(requireContext());
        rvFoods.setAdapter(adapter);

        adapter.setOnItemClickListener(new StorefrontAdapter.OnItemClickListener() {
            @Override
            public void onFoodClick(com.example.fooddelivery.data.model.FoodItem item) {
                Bundle bundle = new Bundle();
                bundle.putLong("food_id", item.getId());
                Navigation.findNavController(view)
                        .navigate(R.id.action_restaurantDetail_to_foodDetail, bundle);
            }

            @Override
            public void onAddToCartClick(com.example.fooddelivery.data.model.FoodItem item) {
                Toast.makeText(requireContext(),
                        "Đã thêm " + item.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe + load data
        viewModel.getFoods().observe(getViewLifecycleOwner(), items ->
                adapter.submitList(items));
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        long restaurantId = getArguments() != null
                ? getArguments().getLong("restaurant_id", -1L)
                : -1L;
        viewModel.loadRestaurantFoods(restaurantId);
    }
}
