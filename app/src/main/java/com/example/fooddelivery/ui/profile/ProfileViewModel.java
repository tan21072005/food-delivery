package com.example.fooddelivery.ui.profile;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.repository.UserRepository;
import com.example.fooddelivery.utils.Constants;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> uploadStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<User> getUser() { return user; }
    public LiveData<String> getUploadStatus() { return uploadStatus; }
    public LiveData<Boolean> getUploadSuccess() { return uploadSuccess; }
    public LiveData<String> getError() { return error; }

    public String getUserName() {
        return sessionManager.getUserName();
    }

    public void loadUserInfo() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            loadUserInfoByEmail();
            return;
        }

        userRepository.getUserById("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    user.setValue(response.body().get(0));
                } else {
                    loadUserInfoByEmail();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                error.setValue("Lỗi tải thông tin: " + t.getMessage());
            }
        });
    }

    private void loadUserInfoByEmail() {
        String email = sessionManager.getEmail();
        if (email == null || email.isEmpty()) {
            user.setValue(sessionFallbackUser());
            return;
        }

        userRepository.getUserByEmail("eq." + email).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    user.setValue(response.body().get(0));
                } else {
                    user.setValue(sessionFallbackUser());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                user.setValue(sessionFallbackUser());
                error.setValue("Lỗi tải thông tin: " + t.getMessage());
            }
        });
    }

    private User sessionFallbackUser() {
        User fallback = new User();
        fallback.setId(sessionManager.getUserId());
        fallback.setUsername(sessionManager.getUserName());
        fallback.setEmail(sessionManager.getEmail());
        fallback.setRole(sessionManager.getRole());
        return fallback;
    }

    public void uploadAvatar(RequestBody requestBody, String fileName) {
        uploadStatus.setValue("Đang tải ảnh lên...");
        userRepository.uploadFile("avatars", fileName, requestBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String publicUrl = Constants.SUPABASE_URL + "storage/v1/object/public/avatars/" + fileName;
                    updateUserAvatarInDb(publicUrl);
                } else {
                    error.setValue("Lỗi tải ảnh: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void updateUserAvatarInDb(String publicUrl) {
        User updateData = new User();
        updateData.setAvatarUrl(publicUrl);
        String eqId = "eq." + sessionManager.getUserId();

        userRepository.updateUser(eqId, updateData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    uploadSuccess.setValue(true);
                    loadUserInfo(); // Reload data
                } else {
                    error.setValue("Cập nhật DB lỗi");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.setValue("Lỗi cập nhật DB: " + t.getMessage());
            }
        });
    }
}
