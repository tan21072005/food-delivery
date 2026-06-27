package com.example.fooddelivery.data.repository;

import androidx.annotation.NonNull;

import com.example.fooddelivery.data.remote.PasswordRecoveryApiClient;
import com.example.fooddelivery.data.remote.apis.PasswordRecoveryApiService;
import com.example.fooddelivery.data.remote.request.PasswordRecoveryRequests;
import com.example.fooddelivery.data.remote.response.AuthResponse;
import com.example.fooddelivery.utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordRecoveryRepository {
    public interface ResultCallback<T> {
        void onSuccess(T value);
        void onError(RecoveryError error);
    }

    public static final class RecoveryError {
        private final int statusCode;
        private final String userMessage;

        public RecoveryError(int statusCode, String userMessage) {
            this.statusCode = statusCode;
            this.userMessage = userMessage;
        }

        public int statusCode() {
            return statusCode;
        }

        public String userMessage() {
            return userMessage;
        }
    }

    private final PasswordRecoveryApiService api;
    private final String anonKey;

    public PasswordRecoveryRepository() {
        this(PasswordRecoveryApiClient.create(), Constants.SUPABASE_ANON_KEY);
    }

    public PasswordRecoveryRepository(PasswordRecoveryApiService api, String anonKey) {
        this.api = api;
        this.anonKey = anonKey;
    }

    public void sendCode(String email, ResultCallback<Void> callback) {
        api.sendCode(anonBearer(), new PasswordRecoveryRequests.Email(email))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call,
                                           @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(mapError(response.code(), false));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                        callback.onError(networkError());
                    }
                });
    }

    public void verifyOtp(String email, String otp, ResultCallback<String> callback) {
        api.verifyOtp(anonBearer(), new PasswordRecoveryRequests.VerifyOtp(email, otp))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null
                                && body.accessToken != null && !body.accessToken.trim().isEmpty()) {
                            callback.onSuccess(body.accessToken);
                        } else if (response.isSuccessful()) {
                            callback.onError(new RecoveryError(
                                    response.code(),
                                    "Không nhận được phiên khôi phục. Vui lòng thử lại"));
                        } else {
                            callback.onError(mapError(response.code(), true));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                          @NonNull Throwable throwable) {
                        callback.onError(networkError());
                    }
                });
    }

    public void updatePassword(String recoveryToken, String password,
                               ResultCallback<Void> callback) {
        api.updatePassword(
                        "Bearer " + recoveryToken,
                        new PasswordRecoveryRequests.NewPassword(password))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(mapError(response.code(), false));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call,
                                          @NonNull Throwable throwable) {
                        callback.onError(networkError());
                    }
                });
    }

    private String anonBearer() {
        return "Bearer " + anonKey;
    }

    private RecoveryError mapError(int statusCode, boolean otpOperation) {
        if (otpOperation && (statusCode == 400 || statusCode == 401 || statusCode == 403)) {
            return new RecoveryError(statusCode,
                    "Mã xác minh không đúng hoặc đã hết hạn");
        }
        if (statusCode == 429) {
            return new RecoveryError(statusCode,
                    "Bạn thao tác quá nhanh. Vui lòng thử lại sau");
        }
        if (statusCode == 401 || statusCode == 403) {
            return new RecoveryError(statusCode,
                    "Phiên khôi phục đã hết hạn. Vui lòng thực hiện lại");
        }
        return new RecoveryError(statusCode,
                "Không thể xử lý yêu cầu. Vui lòng thử lại");
    }

    private RecoveryError networkError() {
        return new RecoveryError(0,
                "Không thể kết nối. Vui lòng kiểm tra mạng");
    }
}
