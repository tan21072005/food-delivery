package com.example.fooddelivery.ui.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.RestaurantInfo;
import com.example.fooddelivery.data.repository.RestaurantRepository;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantInfoFragment extends Fragment {
    private RestaurantRepository restaurantRepository;
    private long restaurantId = -1L;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restaurantRepository = new RestaurantRepository(requireContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        restaurantId = getArguments() != null
                ? getArguments().getLong("restaurant_id", -1L)
                : -1L;

        showLoading(view);
        if (restaurantId <= 0) {
            showFallback(view, "Khong tim thay Restaurant");
            return;
        }
        loadRestaurantInfo(view);
    }

    private void loadRestaurantInfo(View view) {
        restaurantRepository.getRestaurantInfo(restaurantId).enqueue(new Callback<List<RestaurantInfo>>() {
            @Override
            public void onResponse(@NonNull Call<List<RestaurantInfo>> call,
                                   @NonNull Response<List<RestaurantInfo>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    renderRestaurantInfo(view, response.body().get(0));
                } else {
                    showFallback(view, "Chua co thong tin Restaurant");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RestaurantInfo>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showFallback(view, "Khong tai duoc thong tin Restaurant");
            }
        });
    }

    private void renderRestaurantInfo(View view, RestaurantInfo restaurant) {
        setText(view, R.id.tvInfoName, safeText(restaurant.getName(), "Restaurant"));
        setText(view, R.id.tvInfoDescription, safeText(restaurant.getDescription(), "Chua co mo ta"));
        setText(view, R.id.tvInfoAddress, safeText(restaurant.getAddress(), "Chua co dia chi"));
        setText(view, R.id.tvInfoOpenStatus, restaurant.isOpen() ? "Dang mo cua" : "Dang dong cua");

        double rating = restaurant.getAvgRating();
        int totalReviews = restaurant.getTotalReviews();
        setText(view, R.id.tvBigRating, String.format(Locale.US, "%.1f", rating));
        setText(view, R.id.tvRatingCount, totalReviews + " danh gia");
        renderStars(view, rating);
        renderRatingFallback(view, totalReviews);
        loadCoverImage(view, restaurant);
    }

    private void showLoading(View view) {
        setText(view, R.id.tvInfoName, "Dang tai...");
        setText(view, R.id.tvInfoDescription, "");
        setText(view, R.id.tvInfoAddress, "");
        setText(view, R.id.tvInfoOpenStatus, "");
        setText(view, R.id.tvBigRating, "0.0");
        setText(view, R.id.tvRatingCount, "0 danh gia");
        renderStars(view, 0);
        renderRatingFallback(view, 0);
    }

    private void showFallback(View view, String message) {
        setText(view, R.id.tvInfoName, message);
        setText(view, R.id.tvInfoDescription, "");
        setText(view, R.id.tvInfoAddress, "Vui long thu lai sau");
        setText(view, R.id.tvInfoOpenStatus, "");
        setText(view, R.id.tvBigRating, "0.0");
        setText(view, R.id.tvRatingCount, "0 danh gia");
        renderStars(view, 0);
        renderRatingFallback(view, 0);
        ImageView cover = view.findViewById(R.id.ivRestaurantCover);
        if (cover != null) {
            cover.setImageResource(R.drawable.placeholder_banner);
        }
    }

    private void loadCoverImage(View view, RestaurantInfo restaurant) {
        ImageView cover = view.findViewById(R.id.ivRestaurantCover);
        if (cover == null) return;

        String imageUrl = firstNonBlank(restaurant.getCoverUrl(), restaurant.getLogoUrl());
        Glide.with(this)
                .load(isBlank(imageUrl) ? R.drawable.placeholder_banner : imageUrl)
                .placeholder(R.drawable.placeholder_banner)
                .error(R.drawable.placeholder_banner)
                .into(cover);
    }

    private void renderStars(View view, double rating) {
        int filledStars = (int) Math.round(Math.max(0, Math.min(5, rating)));
        int activeColor = Color.parseColor("#F5A623");
        int inactiveColor = Color.parseColor("#E0E0E0");
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        for (int i = 0; i < starIds.length; i++) {
            ImageView star = view.findViewById(starIds[i]);
            if (star != null) {
                star.setColorFilter(i < filledStars ? activeColor : inactiveColor);
            }
        }
    }

    private void renderRatingFallback(View view, int totalReviews) {
        int progress = totalReviews > 0 ? 100 : 0;
        int[] barIds = {R.id.bar5, R.id.bar4, R.id.bar3, R.id.bar2, R.id.bar1};
        for (int id : barIds) {
            ProgressBar bar = view.findViewById(id);
            if (bar != null) {
                bar.setProgress(id == R.id.bar5 ? progress : 0);
            }
        }

        setText(view, R.id.tvBar5Count, String.valueOf(totalReviews));
        setText(view, R.id.tvBar4Count, "0");
        setText(view, R.id.tvBar3Count, "0");
        setText(view, R.id.tvBar2Count, "0");
        setText(view, R.id.tvBar1Count, "0");
    }

    private void setText(View view, int textViewId, String value) {
        TextView textView = view.findViewById(textViewId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
