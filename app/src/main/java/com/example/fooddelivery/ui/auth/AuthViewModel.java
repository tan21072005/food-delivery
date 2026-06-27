package com.example.fooddelivery.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.remote.response.AuthResponse;
import com.example.fooddelivery.data.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>(false);

    private final AuthRepository authRepository;
    private final SessionManager sessionManager;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public void signIn(String email, String password) {
        isLoading.setValue(true);
        error.setValue(null);
        authRepository.signIn(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                AuthResponse body = response.body();
                if (response.isSuccessful() && body != null && body.accessToken != null) {
                    sessionManager.saveSession(body.accessToken, -1, email, email, "customer");
                    loginSuccess.setValue(true);
                } else {
                    error.setValue("Dang nhap that bai: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }

    public void signUp(String email, String password) {
        isLoading.setValue(true);
        error.setValue(null);
        authRepository.signUp(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    signupSuccess.setValue(true);
                } else {
                    error.setValue("Dang ky that bai: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }
}
