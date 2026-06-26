package com.example.fooddelivery.ui.auth;

import com.example.fooddelivery.MainActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fooddelivery.R;

import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.navigation.Navigation;



public class SignUpFragment extends Fragment {

    private EditText edEmail, edPassword, edAgainPassword;
    private Button btnSignUp;
    private TextView tvSignIn;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auth_fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edEmail        = view.findViewById(R.id.edUsername);      // field email
        edPassword     = view.findViewById(R.id.edPassword);
        edAgainPassword = view.findViewById(R.id.edAgianPassword); // lưu ý typo "Agian"
        btnSignUp      = view.findViewById(R.id.btnSignUp);
        tvSignIn       = view.findViewById(R.id.tvSignIn);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSignUp.setEnabled(isLoading == null || !isLoading);
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getSignupSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            String email    = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();
            String confirm  = edAgainPassword.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(email)) {
                edEmail.setError("Email không được trống");
                edEmail.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edPassword.setError("Mật khẩu không được trống");
                edPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edPassword.setError("Mật khẩu tối thiểu 6 ký tự");
                edPassword.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                edAgainPassword.setError("Mật khẩu không khớp");
                edAgainPassword.requestFocus();
                return;
            }

            authViewModel.signUp(email, password);
        });

        // Bấm "Đăng nhập" → quay lại Login
        tvSignIn.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }
}


