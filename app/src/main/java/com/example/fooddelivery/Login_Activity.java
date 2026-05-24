package com.example.fooddelivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

public class Login_Activity extends AppCompatActivity {
    //khai báo hằng số
    public static  final String FULL_NAME ="fullname";
    public static final String TAG = Login_Activity.class.getSimpleName();
    private Button btnLogin,btnlogout;
    private TextView tvSignup,tvchaomuwng,tvname, tvForgetpass;
    private EditText edUsername,edPassword;
    private LoginButton fbLoginButton;

    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100; // Mã định danh cho yêu cầu Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // Khởi tạo Google SignIn Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                // Dán cái mã Web Client ID (dạng xxxx.apps.googleusercontent.com) vào đây
                .requestIdToken("536215355416-7nod5emba775megcu5bk7vmmp9b20f1v.apps.googleusercontent.com")
                .build();

        // lấy mã key hash
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.learnmobileapp", // Đảm bảo đúng Package Name của bạn
                    android.content.pm.PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT));
            }
        } catch (android.content.pm.PackageManager.NameNotFoundException | java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


// Tạo GoogleSignInClient với các tùy chọn trên
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sự kiện khi bấm vào nút Google (ví dụ ID là btn_google_login)
        findViewById(R.id.btn_google_login).setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        btnLogin =findViewById(R.id.btnLogin);
        edPassword=findViewById(R.id.edPassword);
        edUsername=findViewById(R.id.edUsername);
        callbackManager = CallbackManager.Factory.create();
        // tắt bảo mật khi nhập mật khẩu (cho phép trình chiếu/quay phim)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Kích hoạt ghi nhận sự kiện mở app
        // Sửa dòng này trong onCreate của Login_Activity
        AppEventsLogger.activateApp(getApplication());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1> lay ve du lieu trong giao dien
                String username = edUsername.getText().toString().trim();
                String passwoord =edPassword.getText().toString().trim();
                //2.TK dữ liệu đã nhập đúng hay chưa
                if(TextUtils.isEmpty(username)){
                    edUsername.setError("Tên đang nhập còn trống");
                    edUsername.requestFocus();
                } else if (TextUtils.isEmpty(passwoord)) {
                    edPassword.setError("Mật khẩu còn trống");
                    edPassword.requestFocus();

                } else if (username.equals("nhattan")&& passwoord.equals("123")) {
                    //call api
                    // //Request - nhan ve Response: id, fullname, email, phone number, avatar, token.
                    Toast.makeText(Login_Activity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();
                    //khai vao intent
                    Intent intent=new Intent(Login_Activity.this, MainActivity.class);
//                    intent.putExtra(FULL_NAME,"Đại học xây dựng");
                    startActivity(intent);
                    // đóng activity loginn, khi dăng nhập thành công thì chỉ đăng nhập 1 lần à xong
                    // không cần dăng nhập lại
                    finish();
                }
                else{
                    Toast.makeText(Login_Activity.this,"Đăng nhập thất bại ",Toast.LENGTH_SHORT).show();
                }

            }
        });
//        Chuyen sang man hinh quen khau
        tvForgetpass = findViewById(R.id.tvForgetpass);
        tvForgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(Login_Activity.this, Forget_Pass.class);
                startActivity(intent3);
            }
        });

        tvSignup = findViewById(R.id.tvSignup);
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Login_Activity.this, SignUp_Activity.class);
                startActivity(intent1);
            }
        });
        //  ----------------------------------------------------------//
        // nếu đã đăng nhập rồi thì bay thẳng vào trang chủ luôn,
        // không nhìn thấy cái nút Log out ở trang đăng nhập nữa:
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            // Nếu đã login rồi thì đi thẳng vào trong luôn
            startActivity(new Intent(this, MenuActivity.class));
            finish();
        }

        fbLoginButton= findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions("email");
        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "=====FB login ====");
                Log.d(TAG, "FB access token" + loginResult.getAccessToken().getToken());
                Toast.makeText(Login_Activity.this, "Đăng nhập FB thành công", Toast.LENGTH_SHORT).show();

                //khai vao intent
                Intent intent=new Intent(Login_Activity.this, MenuActivity.class);
//                    intent.putExtra(FULL_NAME,"Đại học xây dựng");
                startActivity(intent);
                // đóng activity loginn, khi dăng nhập thành công thì chỉ đăng nhập 1 lần à xong
                // không cần dăng nhập lại
                finish();

            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(Login_Activity.this, "Huỷ đăng nhập FB", Toast.LENGTH_SHORT).show();
            }

            //
            @Override
            public void onError(FacebookException exception) {
                // ĐÂY LÀ ĐOẠN CODE QUAN TRỌNG ĐỂ HIỆN LỖI TRONG LOGCAT
                Log.e("FB_LOGIN_ERROR", "Lỗi chi tiết: " + exception.getMessage());

                if (exception instanceof FacebookAuthorizationException) {
                    Log.e("FB_LOGIN_ERROR", "Có thể do lỗi cấu hình App ID hoặc Key Hash");
                }
            }



        });

    }
    // để onActivityResult nằm ngoài onCreate nếu không sẽ lỗi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Đăng nhập thành công!
                String name = account.getDisplayName();
                Toast.makeText(this, "Chào mừng " + name, Toast.LENGTH_SHORT).show();

                // Chuyển sang màn hình chính (Main.class)
                startActivity(new Intent(this, MenuActivity.class));
                finish();
            } catch (ApiException e) {
                Log.e("GoogleError", "Lỗi: " + e.getStatusCode());
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getFbInfo() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(final JSONObject me, GraphResponse response) {
                            if (me != null) {
                                Log.i("Login: ", me.optString("name"));
                                Log.i("ID: ", me.optString("id"));

                                Toast.makeText(Login_Activity.this, "Name: " + me.optString("name"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(Login_Activity.this, "ID: " + me.optString("id"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }


}

