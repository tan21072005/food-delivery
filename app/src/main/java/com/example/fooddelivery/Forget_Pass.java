package com.example.fooddelivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Forget_Pass extends AppCompatActivity {

    private Button btnNext;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        btnNext = findViewById(R.id.btnNext);
        tvBack = findViewById(R.id.tvBack);

        btnNext.setOnClickListener(v -> {
            // TODO: xử lý tiếp theo (chuyển sang màn nhập OTP)
        });

        tvBack.setOnClickListener(v -> {
            finish(); // quay lại màn trước
        });
    }
}