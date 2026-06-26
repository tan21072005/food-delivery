package com.example.fooddelivery.ui.auth;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.AuthApiService;
import com.example.fooddelivery.data.remote.response.AuthRequest;
import com.example.fooddelivery.data.remote.response.AuthResponse;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    private EditText edEmail, edPassword, edAgainPassword;
    private Button btnSignUp;
    private TextView tvSignIn;

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

            doSignUp(email, password);
        });

        // Bấm "Đăng nhập" → quay lại Login
        tvSignIn.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void doSignUp(String email, String password) {
        btnSignUp.setEnabled(false);

        AuthApiService api = SupabaseClient.getInstance(requireContext()).create(AuthApiService.class);
        api.signUp(new AuthRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignUp.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                    // Quay lại màn Login
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(),
                            "Email đã tồn tại hoặc không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Toast.makeText(getContext(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


