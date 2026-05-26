package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ForgetPassFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forget_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        EditText edEmailPhone = view.findViewById(R.id.edEmailPhone);
        Button btnNext        = view.findViewById(R.id.btnNext);
        TextView tvBack        = view.findViewById(R.id.tvBack);

        btnNext.setOnClickListener(v -> {
            String email = edEmailPhone.getText().toString().trim();
            if (email.isEmpty()) {
                edEmailPhone.setError("Vui lòng nhập email hoặc số điện thoại");
                edEmailPhone.requestFocus();
            } else {
                navController.navigate(R.id.action_forget_to_otp);
            }
        });

        tvBack.setOnClickListener(v -> navController.popBackStack());
    }
}