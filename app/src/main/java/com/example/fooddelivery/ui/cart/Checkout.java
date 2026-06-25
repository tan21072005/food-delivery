package com.example.fooddelivery.ui.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddelivery.R;
import com.example.fooddelivery.MainActivity;

public class Checkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);

        ImageView ivBack = findViewById(R.id.ivBack);
        Button btnOrder = findViewById(R.id.btnOrder);
        TextView tvSchedule = findViewById(R.id.tvSchedule);
        TextView tvAddMore = findViewById(R.id.tvAddMore);
        TextView tvViewAll = findViewById(R.id.tvViewAll);


        ivBack.setOnClickListener(v -> finish());


        tvSchedule.setOnClickListener(v -> {

        });


        tvAddMore.setOnClickListener(v -> {
            finish(); // quay l?i m�n menu d? th�m m�n
        });


        tvViewAll.setOnClickListener(v -> {

        });


        btnOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Thong bao ")
                    .setMessage("Dat mon thanh cong")
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();

                        Intent intent = new Intent(Checkout.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }
}