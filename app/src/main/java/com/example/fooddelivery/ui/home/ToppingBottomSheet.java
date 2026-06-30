package com.example.fooddelivery.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.utils.MoneyFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ToppingBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ToppingBottomSheet";

    private FoodItem foodItem;
    private OnAddToCartListener listener;

    public interface OnAddToCartListener {
        void onAddToCart(FoodItem item, String note, ToppingBottomSheet sheet);
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
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;

        sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvToppingTitle);
        TextView tvTotal = view.findViewById(R.id.tvToppingTotal);
        TextView tvClose = view.findViewById(R.id.tvCloseTopping);
        EditText etNote = view.findViewById(R.id.etToppingNote);
        Button btnAdd = view.findViewById(R.id.btnAddToppingCart);

        if (foodItem != null) {
            tvTitle.setText(foodItem.getName());
            tvTotal.setText(MoneyFormatter.format(foodItem.getPrice()));
        }

        View footer = view.findViewById(R.id.toppingFooter);
        int originalFooterBottomPadding = footer.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(footer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    originalFooterBottomPadding + systemBars.bottom
            );
            return insets;
        });

        tvClose.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            if (listener != null && foodItem != null) {
                String note = etNote == null ? null : etNote.getText().toString();
                listener.onAddToCart(foodItem, note, this);
            }
        });
    }
}
