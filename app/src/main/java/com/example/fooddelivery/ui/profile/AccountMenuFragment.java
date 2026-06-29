package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fooddelivery.R;
import com.google.android.material.snackbar.Snackbar;

public class AccountMenuFragment extends Fragment {

    public static final String PASSWORD_CHANGED_RESULT = "password_changed_result";

    public AccountMenuFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        view.findViewById(R.id.rowAccountInfo).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_accountMenu_to_accountInfo)
        );

        view.findViewById(R.id.rowPassword).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_accountMenu_to_changePassword)
        );

        NavBackStackEntry entry = NavHostFragment.findNavController(this).getCurrentBackStackEntry();
        if (entry != null) {
            entry.getSavedStateHandle().<Boolean>getLiveData(PASSWORD_CHANGED_RESULT)
                    .observe(getViewLifecycleOwner(), changed -> {
                        if (!Boolean.TRUE.equals(changed)) {
                            return;
                        }
                        Snackbar.make(view, "Mật khẩu đã được thay đổi", Snackbar.LENGTH_SHORT).show();
                        entry.getSavedStateHandle().remove(PASSWORD_CHANGED_RESULT);
                    });
        }
    }
}
