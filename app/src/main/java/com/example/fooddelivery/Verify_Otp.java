package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Verify_Otp extends AppCompatActivity {

    // Mã OTP giả lập (thực tế lấy từ server)
    private static final String CORRECT_OTP = "094118";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_verify_otp);

        EditText edOtp = findViewById(R.id.edOtp);
        Button btnSubmitOtp = findViewById(R.id.btnSubmitOtp);
        TextView tvResendCode = findViewById(R.id.tvResendCode);
        TextView tvChangeEmail = findViewById(R.id.tvChangeEmail);
        TextView tvCantAccessEmail = findViewById(R.id.tvCantAccessEmail);

        btnSubmitOtp.setOnClickListener(v -> {
            String otp = edOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                edOtp.setError("Vui lòng nhập mã xác minh");
                edOtp.requestFocus();
            } else if (otp.length() < 6) {
                edOtp.setError("Mã phải gồm 6 chữ số");
                edOtp.requestFocus();
            } else if (otp.equals(CORRECT_OTP)) {
                Toast.makeText(this, "Mã chính xác", Toast.LENGTH_SHORT).show();
                // TODO: chuyển sang màn đặt lại mật khẩu
                // Intent intent = new Intent(this, ResetPassword_Activity.class);
                // startActivity(intent);
                // finish();
            } else {
                Toast.makeText(this, "Mã không đúng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                edOtp.setText("");
                edOtp.requestFocus();
            }
        });

        tvResendCode.setOnClickListener(v ->
                Toast.makeText(this, "Đã gửi lại mã!", Toast.LENGTH_SHORT).show()
        );

        tvChangeEmail.setOnClickListener(v -> finish());

        tvCantAccessEmail.setOnClickListener(v ->
                Toast.makeText(this, "Vui lòng liên hệ hỗ trợ!", Toast.LENGTH_SHORT).show()
        );
    }
}