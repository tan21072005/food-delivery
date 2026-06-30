package com.example.fooddelivery.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.remote.response.AuthErrorParser;
import com.example.fooddelivery.data.remote.response.AuthResponse;
import com.example.fooddelivery.data.repository.AuthRepository;
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
        authRepository.signIn(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                AuthResponse authResponse = response.body();
                if (response.isSuccessful() && hasAuthenticatedSession(authResponse)) {
                    completeAuthenticatedSession(
                            authResponse.accessToken,
                            authResponse.user.id,
                            resolveEmail(authResponse, email),
                            () -> loginSuccess.setValue(true)
                    );
                } else {
                    isLoading.setValue(false);
                    error.setValue("Sai email ho\u1eb7c m\u1eadt kh\u1ea9u");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(AuthErrorParser.networkMessage(t));
            }
        });
    }

    public void signUp(String email, String password) {
        isLoading.setValue(true);
        authRepository.signUp(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isLoading.setValue(false);
                    signupSuccess.setValue(true);
                } else {
                    isLoading.setValue(false);
                    error.setValue(AuthErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(AuthErrorParser.networkMessage(t));
            }
        });
    }

    private void completeAuthenticatedSession(
            String token,
            String authUid,
            String email,
            AuthSuccessEmitter successEmitter
    ) {
        sessionManager.saveSession(token, -1, "", "", "customer");

        userRepository.getUserByAuthUid("eq." + authUid).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    saveResolvedSession(token, response.body().get(0));
                    isLoading.setValue(false);
                    successEmitter.emit();
                } else {
                    completeSessionFromEmail(token, email, successEmitter);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                completeSessionFallback(token, email, successEmitter);
            }
        });
    }

    private void completeSessionFromEmail(String token, String email, AuthSuccessEmitter successEmitter) {
        userRepository.getUserByEmail("eq." + email).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    saveResolvedSession(token, response.body().get(0));
                } else {
                    saveFallbackSession(token, email);
                }
                isLoading.setValue(false);
                successEmitter.emit();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                completeSessionFallback(token, email, successEmitter);
            }
        });
    }

    private void completeSessionFallback(String token, String email, AuthSuccessEmitter successEmitter) {
        saveFallbackSession(token, email);
        isLoading.setValue(false);
        successEmitter.emit();
    }

    private void saveResolvedSession(String token, User dbUser) {
        sessionManager.saveSession(
                token,
                (int) dbUser.getId(),
                dbUser.getUsername(),
                dbUser.getEmail(),
                dbUser.getRole()
        );
    }

    private void saveFallbackSession(String token, String email) {
        sessionManager.saveSession(token, -1, email, email, "customer");
    }

    private boolean hasAuthenticatedSession(AuthResponse authResponse) {
        return authResponse != null
                && authResponse.accessToken != null
                && !authResponse.accessToken.trim().isEmpty()
                && authResponse.user != null
                && authResponse.user.id != null
                && !authResponse.user.id.trim().isEmpty();
    }

    private String resolveEmail(AuthResponse authResponse, String fallbackEmail) {
        if (authResponse != null
                && authResponse.user != null
                && authResponse.user.email != null
                && !authResponse.user.email.trim().isEmpty()) {
            return authResponse.user.email;
        }
        return fallbackEmail;
    }

    private interface AuthSuccessEmitter {
        void emit();
    }
}
