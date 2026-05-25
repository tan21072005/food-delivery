package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private Button btnLogin;
    private EditText edUsername, edPassword;
    private TextView tvSignup;
    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    // ─── KHÁC Activity: inflate layout ───
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    // ─── KHÁC Activity: ánh xạ view trong onViewCreated ───
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // KHÁC Activity: phải có "view." ở trước
        btnLogin      = view.findViewById(R.id.btnLogin);
        edUsername    = view.findViewById(R.id.edUsername);
        edPassword    = view.findViewById(R.id.edPassword);
        tvSignup      = view.findViewById(R.id.tvSignup);
        fbLoginButton = view.findViewById(R.id.login_button);

        setupGoogle(view);
        setupFacebook();
        setupLogin();
        setupSignup();
        checkAlreadyLoggedIn();
    }

    // ─────────────────────────────────────────────────────────
    // Google Sign-In
    // ─────────────────────────────────────────────────────────
    private void setupGoogle(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("536215355416-7nod5emba775megcu5bk7vmmp9b20f1v.apps.googleusercontent.com")
                .build();

        // KHÁC Activity: dùng requireActivity() thay vì this
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        view.findViewById(R.id.btn_google_login).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // lấy mã key hash (giữ nguyên logic)
        try {
            android.content.pm.PackageInfo info = requireActivity()
                    .getPackageManager()
                    .getPackageInfo(
                            "com.example.fooddelivery",
                            android.content.pm.PackageManager.GET_SIGNATURES
                    );
            for (android.content.pm.Signature signature : info.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    // Facebook Login
    // ─────────────────────────────────────────────────────────
    private void setupFacebook() {
        callbackManager = CallbackManager.Factory.create();

        fbLoginButton.setReadPermissions("email");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "FB access token: " + loginResult.getAccessToken().getToken());
                // KHÁC Activity: dùng requireContext() thay vì Login_Activity.this
                Toast.makeText(requireContext(), "Đăng nhập FB thành công", Toast.LENGTH_SHORT).show();

                // KHÁC Activity: dùng Navigation thay vì startActivity + Intent
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_home);
            }

            @Override
            public void onCancel() {
                Toast.makeText(requireContext(), "Huỷ đăng nhập FB", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException exception) {
                Log.e(TAG, "Lỗi FB: " + exception.getMessage());
                if (exception instanceof FacebookAuthorizationException) {
                    Log.e(TAG, "Lỗi cấu hình App ID hoặc Key Hash");
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // Đăng nhập thường
    // ─────────────────────────────────────────────────────────
    private void setupLogin() {
        btnLogin.setOnClickListener(v -> {
            String username = edUsername.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                edUsername.setError("Tên đăng nhập còn trống");
                edUsername.requestFocus();
            } else if (TextUtils.isEmpty(password)) {
                edPassword.setError("Mật khẩu còn trống");
                edPassword.requestFocus();
            } else if (username.equals("nhattan") && password.equals("123")) {
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // KHÁC Activity: Navigation thay vì Intent
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_home);
            } else {
                Toast.makeText(requireContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // Chuyển sang màn hình Đăng ký
    // ─────────────────────────────────────────────────────────
    private void setupSignup() {
        tvSignup.setOnClickListener(v ->
                // KHÁC Activity: Navigation thay vì Intent
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_signup)
        );
    }

    // ─────────────────────────────────────────────────────────
    // Nếu đã đăng nhập Facebook rồi → vào thẳng trang chủ
    // ─────────────────────────────────────────────────────────
    private void checkAlreadyLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_login_to_home);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Bắt buộc để Facebook và Google SDK hoạt động
    // ─────────────────────────────────────────────────────────
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String name = account.getDisplayName();
                Toast.makeText(requireContext(), "Chào mừng " + name, Toast.LENGTH_SHORT).show();

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_home);
            } catch (ApiException e) {
                Log.e(TAG, "Lỗi Google: " + e.getStatusCode());
                Toast.makeText(requireContext(), "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}