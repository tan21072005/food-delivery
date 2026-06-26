package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SwitchAccountBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "SwitchAccountBottomSheet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.switch_account_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.account1).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã chuyển sang tài khoản: Food Delivery", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.account2).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã chuyển sang tài khoản: Tài khoản khác", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.tvAddAccount).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng thêm tài khoản chưa được cài đặt", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
