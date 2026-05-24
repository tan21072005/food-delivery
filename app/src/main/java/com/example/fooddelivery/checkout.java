package com.example.fooddelivery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class checkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkouut);

        ImageView ivBack = findViewById(R.id.ivBack);
        Button btnOrder = findViewById(R.id.btnOrder);
        TextView tvSchedule = findViewById(R.id.tvSchedule);
        TextView tvAddMore = findViewById(R.id.tvAddMore);
        TextView tvViewAll = findViewById(R.id.tvViewAll);

        // Nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Hẹn giờ giao
        tvSchedule.setOnClickListener(v -> {
            // TODO: mở dialog chọn giờ giao
        });

        // Thêm món
        tvAddMore.setOnClickListener(v -> {
            finish(); // quay lại màn menu để thêm món
        });

        // Xem tất cả phương thức thanh toán
        tvViewAll.setOnClickListener(v -> {
            // TODO: mở danh sách phương thức thanh toán
        });

        // Nút Đặt món
        btnOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Đặt món thành công")
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        // Chuyển về màn chính sau khi đặt thành công
                        Intent intent = new Intent(checkout.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }
}