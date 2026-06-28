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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.prefs.SessionManager;

public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private Button btnLogin;
    private EditText edUsername;
    private EditText edPassword;
    private TextView tvSignup;
    private TextView tvForgetpass;
    private View hiddenFacebookButton;
    private NavController navController;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auth_fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        new ViewModelProvider(requireActivity())
                .get(PasswordRecoveryViewModel.class)
                .restart();

        btnLogin      = view.findViewById(R.id.btnLogin);
        edUsername    = view.findViewById(R.id.edUsername);
        edPassword    = view.findViewById(R.id.edPassword);
        tvSignup      = view.findViewById(R.id.tvSignup);
        hiddenFacebookButton = view.findViewById(R.id.login_button);
        tvForgetpass  = view.findViewById(R.id.tvForgetpass);

        observeViewModel();
        setupLogin();
        setupSocialIcons(view);
        setupSignup();
        setupForgotPassword();
        checkAlreadyLoggedIn();
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                btnLogin.setEnabled(isLoading == null || !isLoading)
        );

        authViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Dang nhap thanh cong", Toast.LENGTH_SHORT).show();
                goToMain();
            }
        });
    }

    private void setupLogin() {
        btnLogin.setOnClickListener(v -> {
            String username = edUsername.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                edUsername.setError("Email con trong");
                edUsername.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edPassword.setError("Mat khau con trong");
                edPassword.requestFocus();
                return;
            }

            authViewModel.signIn(username, password);
        });
    }

    private void setupSocialIcons(View view) {
        if (hiddenFacebookButton != null) {
            hiddenFacebookButton.setVisibility(View.GONE);
        }

        View googleIcon = view.findViewById(R.id.btn_google_login);
        if (googleIcon != null) {
            googleIcon.setOnClickListener(v ->
                    authViewModel.signIn("google.local@fooddelivery.app", "local")
            );
        }

        View facebookIcon = view.findViewById(R.id.fbLoginButton);
        if (facebookIcon != null) {
            facebookIcon.setOnClickListener(v ->
                    authViewModel.signIn("facebook.local@fooddelivery.app", "local")
            );
        }
    }

    private void setupSignup() {
        tvSignup.setOnClickListener(v ->
                navController.navigate(R.id.action_login_to_signup)
        );
    }

    private void setupForgotPassword() {
        tvForgetpass.setOnClickListener(v ->
                navController.navigate(R.id.action_login_to_reset_password)
        );
    }

    private void checkAlreadyLoggedIn() {
        SessionManager session = new SessionManager(requireContext());
        if (session.isLoggedIn()) {
            goToMain();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
