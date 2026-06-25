package com.example.fooddelivery.ui.auth;

import com.example.fooddelivery.R;

import static androidx.navigation.Navigation.findNavController;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavAction;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

public class Reset_password extends Fragment {

    boolean isNewPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auth_fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edNewPassword = view.findViewById(R.id.edNewPassword);
        EditText edConfirmPassword = view.findViewById(R.id.edConfirmPassword);
        ImageView ivToggleNew = view.findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm);
        Button btnConfirmReset = view.findViewById(R.id.btnConfirmReset);

        // Toggle hiện/ẩn mật khẩu mới
        ivToggleNew.setOnClickListener(v -> {
            if (isNewPasswordVisible) {
                edNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isNewPasswordVisible = false;
            } else {
                edNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isNewPasswordVisible = true;
            }
            edNewPassword.setSelection(edNewPassword.getText().length());
        });

        // Toggle hiện/ẩn nhập lại mật khẩu
        ivToggleConfirm.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isConfirmPasswordVisible = false;
            } else {
                edConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isConfirmPasswordVisible = true;
            }
            edConfirmPassword.setSelection(edConfirmPassword.getText().length());
        });

        // Xác nhận đổi mật khẩu
        btnConfirmReset.setOnClickListener(v -> {
            String newPass = edNewPassword.getText().toString().trim();
            String confirmPass = edConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPass)) {
                edNewPassword.setError("Vui lòng nhập mật khẩu mới");
                edNewPassword.requestFocus();
            } else if (newPass.length() < 8) {
                edNewPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
                edNewPassword.requestFocus();
            } else if (!newPass.matches(".*[A-Za-z].*") || !newPass.matches(".*[0-9].*") || !newPass.matches(".*[!@#$%^&*()].*")) {
                edNewPassword.setError("Mật khẩu cần có chữ cái, số và ký tự đặc biệt");
                edNewPassword.requestFocus();
            } else if (TextUtils.isEmpty(confirmPass)) {
                edConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
                edConfirmPassword.requestFocus();
            } else if (!newPass.equals(confirmPass)) {
                edConfirmPassword.setError("Mật khẩu không khớp");
                edConfirmPassword.requestFocus();
            } else {
                // taoj dialog moi
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(R.layout.dialog_success)
                        .setCancelable(false)
                        .create();
                // hien thi
                dialog.show();

                //dialog bị xoá sau 2s
                view.postDelayed(() -> {
                    dialog.dismiss();
                    Navigation.findNavController(view).navigate(R.id.action_reset_to_login);
                }, 2000);
            }
        });
    }
}
