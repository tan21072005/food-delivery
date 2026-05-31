package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LogoutBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSwitchAccount();
        void onLogout();
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnSwitchAccount = view.findViewById(R.id.btnSwitchAccountOption);
        TextView btnLogout        = view.findViewById(R.id.btnLogout);
        TextView btnCancel        = view.findViewById(R.id.btnCancel);

        btnSwitchAccount.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onSwitchAccount();
        });
        btnLogout.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onLogout();
        });
        btnCancel.setOnClickListener(v -> dismiss());
    }
}
