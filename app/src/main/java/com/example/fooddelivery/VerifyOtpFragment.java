package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class VerifyOtpFragment extends Fragment {

    private static final String CORRECT_OTP = "1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        EditText edOtp             = view.findViewById(R.id.edOtp);
        Button btnSubmitOtp        = view.findViewById(R.id.btnSubmitOtp);
        TextView tvResendCode      = view.findViewById(R.id.tvResendCode);
        TextView tvChangeEmail     = view.findViewById(R.id.tvChangeEmail);
        TextView tvCantAccessEmail = view.findViewById(R.id.tvCantAccessEmail);

        btnSubmitOtp.setOnClickListener(v -> {
            String otp = edOtp.getText().toString().trim();
//            if (otp.isEmpty()) {
//                edOtp.setError("Vui lòng nhập mã xác minh");
//                edOtp.requestFocus();
//            } else if (otp.length() < 6) {
//                edOtp.setError("Mã phải gồm 6 chữ số");
//                edOtp.requestFocus();
//            } else if (otp.equals(CORRECT_OTP)) {
           if (otp.equals(CORRECT_OTP)) {
                Toast.makeText(requireContext(), "Mã chính xác", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_otp_to_reset);
            } else {
                Toast.makeText(requireContext(), "Mã không đúng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                edOtp.setText("");
                edOtp.requestFocus();
            }
        });

        tvResendCode.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đã gửi lại mã!", Toast.LENGTH_SHORT).show());

        tvChangeEmail.setOnClickListener(v -> navController.popBackStack());

        tvCantAccessEmail.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Vui lòng liên hệ hỗ trợ!", Toast.LENGTH_SHORT).show());
    }
}