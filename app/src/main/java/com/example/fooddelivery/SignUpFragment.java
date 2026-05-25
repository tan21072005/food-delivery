package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SignUpFragment extends Fragment {

    private Button btnSignUp;
    private TextView tvSignUp;

    // KHÁC Activity: dùng onCreateView để inflate layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    // KHÁC Activity: ánh xạ view trong onViewCreated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // KHÁC Activity: phải có "view." ở trước
        btnSignUp = view.findViewById(R.id.btnSignUp);
        tvSignUp  = view.findViewById(R.id.tvSignIn);

        // Bấm nút Đăng ký → quay lại màn hình trước
        btnSignUp.setOnClickListener(v ->
                // KHÁC Activity: dùng popBackStack thay vì finish()
                Navigation.findNavController(v).popBackStack()
        );

        // Bấm "Đã có tài khoản" → quay lại Login
        tvSignUp.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }
}