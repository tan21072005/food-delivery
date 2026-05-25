package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Forget_Pass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forget_password);

        EditText edEmailPhone = findViewById(R.id.edEmailPhone);
        Button btnNext = findViewById(R.id.btnNext);
        TextView tvBack = findViewById(R.id.tvBack);

        btnNext.setOnClickListener(v -> {
            String email = edEmailPhone.getText().toString().trim();
            if (email.isEmpty()) {
                edEmailPhone.setError("Vui lòng nhập email hoặc số điện thoại");
                edEmailPhone.requestFocus();
            } else {
                // Chuyển sang màn nhập OTP
                Intent intent = new Intent(Forget_Pass.this, Verify_Otp.class);
                startActivity(intent);
            }
        });

        tvBack.setOnClickListener(v -> finish());
    }
}