package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LogoutBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "LogoutBottomSheet";

    private final Runnable onLogout;
    private final Runnable onSwitchAccount;

    public LogoutBottomSheet(Runnable onLogout, Runnable onSwitchAccount) {
        this.onLogout = onLogout;
        this.onSwitchAccount = onSwitchAccount;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.logout_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.tvLogout).setOnClickListener(v -> {
            dismiss();
            if (onLogout != null) onLogout.run();
        });

        view.findViewById(R.id.tvSwitchAccount).setOnClickListener(v -> {
            dismiss();
            if (onSwitchAccount != null) onSwitchAccount.run();
        });

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dismiss());
    }
}
