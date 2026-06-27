package com.example.fooddelivery.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;

import okhttp3.RequestBody;

public class ProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> uploadStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>(false);
    private final SessionManager sessionManager;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
    }

    public String getUserName() {
        String name = sessionManager.getUserName();
        return name == null || name.isEmpty() ? "Khach hang" : name;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getUploadStatus() {
        return uploadStatus;
    }

    public LiveData<Boolean> getUploadSuccess() {
        return uploadSuccess;
    }

    public void loadUserInfo() {
        User currentUser = new User();
        currentUser.setUsername(getUserName());
        currentUser.setEmail(sessionManager.getEmail());
        currentUser.setRole(sessionManager.getRole());
        user.setValue(currentUser);
    }

    public void uploadAvatar(RequestBody requestBody, String fileName) {
        uploadStatus.setValue("Da chon anh: " + fileName);
        uploadSuccess.setValue(true);
    }
}
