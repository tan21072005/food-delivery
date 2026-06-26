package com.example.fooddelivery.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MenuBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "MenuBottomSheet";

    private OnCategorySelectListener listener;

    public interface OnCategorySelectListener {
        void onCategorySelected(String categorySlug, String categoryName);
    }

    public void setOnCategorySelectListener(OnCategorySelectListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvClose = view.findViewById(R.id.tvCloseMenuSheet);
        tvClose.setOnClickListener(v -> dismiss());

        View rowBun = view.findViewById(R.id.rowBun);
        View rowFastFood = view.findViewById(R.id.rowFastFood);

        if (rowBun != null) {
            rowBun.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected("bun", "Bún");
                }
                dismiss();
            });
        }

        if (rowFastFood != null) {
            rowFastFood.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected("do-an-nhanh", "Đồ ăn nhanh");
                }
                dismiss();
            });
        }
        
        // Similarly for other categories, you would wire them up here if needed.
    }
}
