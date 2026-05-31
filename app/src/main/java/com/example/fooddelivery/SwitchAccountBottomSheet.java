package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SwitchAccountBottomSheet extends BottomSheetDialogFragment {

    public interface OnAccountSelectedListener {
        void onAccountSelected(String userName);
    }

    private OnAccountSelectedListener listener;
    private String currentUser = "Trần Nhật Tân";

    private static final String ACCOUNT_1 = "Trần Nhật Tân";
    private static final String ACCOUNT_2 = "Ông trùm bom hàng";

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public void setListener(OnAccountSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_switch_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnClose    = view.findViewById(R.id.btnClose);
        LinearLayout account1 = view.findViewById(R.id.accountItem1);
        LinearLayout account2 = view.findViewById(R.id.accountItem2);
        LinearLayout btnAdd   = view.findViewById(R.id.btnAddAccount);
        ImageView ivCheck1    = view.findViewById(R.id.ivCheck1);
        ImageView ivCheck2    = view.findViewById(R.id.ivCheck2);

        updateCheckmarks(ivCheck1, ivCheck2, currentUser);

        btnClose.setOnClickListener(v -> dismiss());

        account1.setOnClickListener(v -> {
            if (!currentUser.equals(ACCOUNT_1)) {
                currentUser = ACCOUNT_1;
                updateCheckmarks(ivCheck1, ivCheck2, currentUser);
                if (listener != null) listener.onAccountSelected(ACCOUNT_1);
                dismiss();
            }
        });

        account2.setOnClickListener(v -> {
            if (!currentUser.equals(ACCOUNT_2)) {
                currentUser = ACCOUNT_2;
                updateCheckmarks(ivCheck1, ivCheck2, currentUser);
                if (listener != null) listener.onAccountSelected(ACCOUNT_2);
                dismiss();
            }
        });

        btnAdd.setOnClickListener(v -> {
            dismiss();
            Toast.makeText(requireContext(), "Thêm tài khoản mới", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCheckmarks(ImageView check1, ImageView check2, String activeUser) {
        check1.setVisibility(activeUser.equals(ACCOUNT_1) ? View.VISIBLE : View.GONE);
        check2.setVisibility(activeUser.equals(ACCOUNT_2) ? View.VISIBLE : View.GONE);
    }
}
