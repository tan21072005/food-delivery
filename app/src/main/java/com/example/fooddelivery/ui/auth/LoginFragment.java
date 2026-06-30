package com.example.fooddelivery.ui.auth;

import com.example.fooddelivery.R;
import com.example.fooddelivery.MainActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
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
import androidx.navigation.NavController;
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

import com.google.android.gms.tasks.Task;


public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private Button btnLogin;
    private EditText edUsername, edPassword;
    private TextView tvSignup, tvForgetpass;
    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
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

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnLogin.setEnabled(isLoading == null || !isLoading);
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "ÄÄƒng nháº­p thÃ nh cÃ´ng!", Toast.LENGTH_SHORT).show();
                goToMain();
            }
        });

        btnLogin      = view.findViewById(R.id.btnLogin);
        edUsername    = view.findViewById(R.id.edUsername);
        edPassword    = view.findViewById(R.id.edPassword);
        tvSignup      = view.findViewById(R.id.tvSignup);
        fbLoginButton = view.findViewById(R.id.login_button);
        tvForgetpass  = view.findViewById(R.id.tvForgetpass);

        setupGoogle(view);
        setupFacebook();
        setupLogin();
        setupSignup();
        setupForgotPassword();
        checkAlreadyLoggedIn();
    }

    // â”€â”€ Chuyá»ƒn sang MainActivity sau khi login thÃ nh cÃ´ng â”€â”€
    private void goToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupGoogle(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("536215355416-7nod5emba775megcu5bk7vmmp9b20f1v.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        view.findViewById(R.id.btn_google_login).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

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

    private void setupFacebook() {
        callbackManager = CallbackManager.Factory.create();

        fbLoginButton.setReadPermissions("email");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "FB access token: " + loginResult.getAccessToken().getToken());
                Toast.makeText(requireContext(), "ÄÄƒng nháº­p FB thÃ nh cÃ´ng", Toast.LENGTH_SHORT).show();
                goToMain(); // â† Intent thay vÃ¬ navController
            }

            @Override
            public void onCancel() {
                Toast.makeText(requireContext(), "Huá»· Ä‘Äƒng nháº­p FB", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException exception) {
                Log.e(TAG, "Lá»—i FB: " + exception.getMessage());
                if (exception instanceof FacebookAuthorizationException) {
                    Log.e(TAG, "Lá»—i cáº¥u hÃ¬nh App ID hoáº·c Key Hash");
                }
            }
        });
    }

    private void setupLogin() {
        btnLogin.setOnClickListener(v -> {
            String username = edUsername.getText().toString().trim();
            String password = edPassword.getText().toString().trim();
//
//            if (TextUtils.isEmpty(username)) {
//                edUsername.setError("TÃªn Ä‘Äƒng nháº­p cÃ²n trá»‘ng");
//                edUsername.requestFocus();
//            } else if (TextUtils.isEmpty(password)) {
//                edPassword.setError("Máº­t kháº©u cÃ²n trá»‘ng");
//                edPassword.requestFocus();
//            } else if (username.equals("nhattan") && password.equals("123")) {
//                Toast.makeText(requireContext(), "ÄÄƒng nháº­p thÃ nh cÃ´ng", Toast.LENGTH_SHORT).show();
//                goToMain(); // â† Intent thay vÃ¬ navController
//            } else {
//                Toast.makeText(requireContext(), "ÄÄƒng nháº­p tháº¥t báº¡i", Toast.LENGTH_SHORT).show();
//            }

            // táº¡i sao ko dÃ¹ng if .... else mÃ  láº¡i dÃ¹ng return
            // phÆ°Æ¡ng phÃ¡p Early Return giÃºp code dá»… Ä‘á»c khÃ´ng bá»‹ lá»“ng sau so vá»›i if.. else
            if(TextUtils.isEmpty(username)){
                edUsername.setError("Email cÃ²n trá»‘ng");
                edUsername.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)){
                edPassword.setError("Máº­t kháº£u cÃ²n trá»‘ng");
                edPassword.requestFocus();
                return;
            }
            // táº¯t nÃºt login trÃ¡nh request nhiá»u láº§n trong kkhi Ä‘ang gá»i API
            authViewModel.signIn(username, password);
        });
    }


    private void setupSignup() {
        tvSignup.setOnClickListener(v ->
                navController.navigate(R.id.action_login_to_signup));
    }

    private void setupForgotPassword() {
        tvForgetpass.setOnClickListener(v ->
                navController.navigate(R.id.action_login_to_reset_password));
    }

    private void checkAlreadyLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            goToMain(); // â† Intent thay vÃ¬ navController
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String name = account.getDisplayName();
                Toast.makeText(requireContext(), "ChÃ o má»«ng " + name, Toast.LENGTH_SHORT).show();
                goToMain(); // â† Intent thay vÃ¬ navController
            } catch (ApiException e) {
                Log.e(TAG, "Lá»—i Google: " + e.getStatusCode());
                Toast.makeText(requireContext(), "ÄÄƒng nháº­p Google tháº¥t báº¡i", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
