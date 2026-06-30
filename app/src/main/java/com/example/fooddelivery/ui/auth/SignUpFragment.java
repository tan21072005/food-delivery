package com.example.fooddelivery.ui.auth;

import android.content.Intent;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.R;

public class SignUpFragment extends Fragment {

    private EditText edEmail;
    private EditText edPassword;
    private EditText edAgainPassword;
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

        edEmail = view.findViewById(R.id.edUsername);
        edPassword = view.findViewById(R.id.edPassword);
        edAgainPassword = view.findViewById(R.id.edAgianPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        tvSignIn = view.findViewById(R.id.tvSignIn);

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
                Toast.makeText(getContext(), "\u0110\u0103ng k\u00fd th\u00e0nh c\u00f4ng!", Toast.LENGTH_LONG).show();
                goToMain();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();
            String confirm = edAgainPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                edEmail.setError("Email kh\u00f4ng \u0111\u01b0\u1ee3c tr\u1ed1ng");
                edEmail.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edPassword.setError("M\u1eadt kh\u1ea9u kh\u00f4ng \u0111\u01b0\u1ee3c tr\u1ed1ng");
                edPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edPassword.setError("M\u1eadt kh\u1ea9u t\u1ed1i thi\u1ec3u 6 k\u00fd t\u1ef1");
                edPassword.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                edAgainPassword.setError("M\u1eadt kh\u1ea9u kh\u00f4ng kh\u1edbp");
                edAgainPassword.requestFocus();
                return;
            }

            authViewModel.signUp(email, password);
        });

        tvSignIn.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void goToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
