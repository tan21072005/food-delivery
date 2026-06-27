package com.example.fooddelivery.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.Order;

public class OrderReviewFragment extends Fragment {

    private ImageView[] starRes;
    private ImageView[] starDriver;
    private int resRating = 0;
    private int driverRating = 0;
    private int foodFeedback = 0;
    private long orderId = -1;
    private Order order;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderId = getArguments() != null ? getArguments().getLong("order_id", -1) : -1;
        if (orderId != -1) {
            order = com.example.fooddelivery.data.local.LocalOrderStore.getInstance().findOrderById(orderId);
        }

        // Back button
        view.findViewById(R.id.ivBack).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });

        // Submit button
        view.findViewById(R.id.btnSubmitReview).setOnClickListener(v -> {
            if (resRating == 0) {
                Toast.makeText(requireContext(), "Vui long danh gia nha hang", Toast.LENGTH_SHORT).show();
                return;
            }
            if (foodFeedback == 0) {
                Toast.makeText(requireContext(), "Vui long danh gia mon an", Toast.LENGTH_SHORT).show();
                return;
            }
            if (driverRating == 0) {
                Toast.makeText(requireContext(), "Vui long danh gia tai xe", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update order status
            if (orderId != -1) {
                com.example.fooddelivery.data.local.LocalOrderStore.getInstance().markAsReviewed(orderId);
            }
            
            // Show success dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đánh giá thành công")
                    .setMessage("Cảm ơn bạn đã gửi đánh giá!")
                    .setPositiveButton("Đóng", (dialog, which) -> {
                        NavController navController = Navigation.findNavController(v);
                        navController.popBackStack();
                    })
                    .show();
        });

        bindOrderFood(view);

        // Init stars
        starRes = new ImageView[]{
                view.findViewById(R.id.starRes1),
                view.findViewById(R.id.starRes2),
                view.findViewById(R.id.starRes3),
                view.findViewById(R.id.starRes4),
                view.findViewById(R.id.starRes5)
        };
        for (int i = 0; i < 5; i++) {
            final int index = i;
            starRes[i].setOnClickListener(v -> setResRating(index + 1));
        }

        starDriver = new ImageView[]{
                view.findViewById(R.id.starDriver1),
                view.findViewById(R.id.starDriver2),
                view.findViewById(R.id.starDriver3),
                view.findViewById(R.id.starDriver4),
                view.findViewById(R.id.starDriver5)
        };
        for (int i = 0; i < 5; i++) {
            final int index = i;
            starDriver[i].setOnClickListener(v -> setDriverRating(index + 1));
        }

        // Thumb up/down
        ImageView ivThumbUp = view.findViewById(R.id.ivThumbUp);
        ImageView ivThumbDown = view.findViewById(R.id.ivThumbDown);
        
        ivThumbUp.setOnClickListener(v -> {
            foodFeedback = 1;
            ivThumbUp.setColorFilter(0xFF1A7A4A); // Green
            ivThumbDown.setColorFilter(0xFF999999); // Gray
        });
        ivThumbDown.setOnClickListener(v -> {
            foodFeedback = -1;
            ivThumbDown.setColorFilter(0xFFD32F2F); // Red
            ivThumbUp.setColorFilter(0xFF999999); // Gray
        });
    }

    private void bindOrderFood(View view) {
        if (order == null) return;

        ImageView imgReviewFood = view.findViewById(R.id.imgReviewFood);
        TextView tvReviewFoodName = view.findViewById(R.id.tvReviewFoodName);

        if (imgReviewFood != null && order.getFoodImageResId() != 0) {
            imgReviewFood.setImageResource(order.getFoodImageResId());
        }
        if (tvReviewFoodName != null) {
            tvReviewFoodName.setText(order.getFoodName());
        }
    }

    private void setResRating(int rating) {
        resRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starRes[i].setImageResource(R.drawable.ic_star);
            } else {
                starRes[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
    }

    private void setDriverRating(int rating) {
        driverRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starDriver[i].setImageResource(R.drawable.ic_star);
            } else {
                starDriver[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
    }
}
