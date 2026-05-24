package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddelivery.Login_Activity;
import com.example.fooddelivery.R;

public class Reset_password extends AppCompatActivity {

    boolean isNewPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reset_password);

        EditText edNewPassword = findViewById(R.id.edNewPassword);
        EditText edConfirmPassword = findViewById(R.id.edConfirmPassword);
        ImageView ivToggleNew = findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = findViewById(R.id.ivToggleConfirm);
        Button btnConfirmReset = findViewById(R.id.btnConfirmReset);

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
                // Thành công
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                // Quay về màn Login
                Intent intent = new Intent(Reset_password.this, Login_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}

