package com.example.fooddelivery.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ToppingBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ToppingBottomSheet";

    private FoodItem foodItem;
    private OnAddToCartListener listener;

    public interface OnAddToCartListener {
        void onAddToCart(FoodItem item);
    }

    public ToppingBottomSheet(FoodItem foodItem, OnAddToCartListener listener) {
        this.foodItem = foodItem;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topping_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvToppingTitle);
        TextView tvTotal = view.findViewById(R.id.tvToppingTotal);
        TextView tvClose = view.findViewById(R.id.tvCloseTopping);
        Button btnAdd = view.findViewById(R.id.btnAddToppingCart);

        if (foodItem != null) {
            tvTitle.setText(foodItem.getName());
            tvTotal.setText(foodItem.getFormattedPrice());
        }

        tvClose.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            if (listener != null && foodItem != null) {
                listener.onAddToCart(foodItem);
            }
            dismiss();
        });
    }
}
