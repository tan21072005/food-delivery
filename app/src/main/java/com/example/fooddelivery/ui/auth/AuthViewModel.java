package com.example.fooddelivery.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.remote.response.AuthResponse;
import com.example.fooddelivery.data.repository.AuthRepository;

import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.repository.UserRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        sessionManager = new SessionManager(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getSignupSuccess() { return signupSuccess; }

    public void signIn(String email, String password) {
        isLoading.setValue(true);
        authRepository.signIn(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().accessToken;
                    String authUid = response.body().user.id;

                    // Lưu tạm token để các request sau (như lấy user info) có JWT
                    sessionManager.saveSession(token, -1, "", "", "customer");

                    userRepository.getUserByAuthUid("eq." + authUid).enqueue(new Callback<List<User>>() {
                        @Override
                        public void onResponse(Call<List<User>> call2, Response<List<User>> response2) {
                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {
                                User dbUser = response2.body().get(0);
                                // Cập nhật SessionManager với User ID (BIGINT) thật
                                sessionManager.saveSession(
                                        token,
                                        (int) dbUser.getId(),
                                        dbUser.getUsername(),
                                        dbUser.getEmail(),
                                        dbUser.getRole()
                                );
                                isLoading.setValue(false);
                                loginSuccess.setValue(true);
                            } else {
                                // Thử fallback tìm theo email
                                userRepository.getUserByEmail("eq." + email).enqueue(new Callback<List<User>>() {
                                    @Override
                                    public void onResponse(Call<List<User>> call3, Response<List<User>> response3) {
                                        isLoading.setValue(false);
                                        if (response3.isSuccessful() && response3.body() != null && !response3.body().isEmpty()) {
                                            User dbUser = response3.body().get(0);
                                            sessionManager.saveSession(token, (int) dbUser.getId(), dbUser.getUsername(), dbUser.getEmail(), dbUser.getRole());
                                        } else {
                                            // Fallback: Vẫn cho đăng nhập nhưng ID = -1 (Lỗi đồng bộ DB)
                                            sessionManager.saveSession(token, -1, email, email, "customer");
                                        }
                                        loginSuccess.setValue(true);
                                    }

                                    @Override
                                    public void onFailure(Call<List<User>> call3, Throwable t3) {
                                        isLoading.setValue(false);
                                        sessionManager.saveSession(token, -1, email, email, "customer");
                                        loginSuccess.setValue(true);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<User>> call2, Throwable t2) {
                            isLoading.setValue(false);
                            // Fallback: Vẫn cho đăng nhập nhưng ID = -1
                            sessionManager.saveSession(token, -1, email, email, "customer");
                            loginSuccess.setValue(true);
                        }
                    });
                } else {
                    isLoading.setValue(false);
                    error.setValue("Sai email hoặc mật khẩu");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void signUp(String email, String password) {
        isLoading.setValue(true);
        authRepository.signUp(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    signupSuccess.setValue(true);
                } else {
                    error.setValue("Đăng ký thất bại");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
