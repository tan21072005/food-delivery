package com.example.fooddelivery.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.repository.UserRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountInfoViewModel extends AndroidViewModel {

    private static final String EMPTY_VALUE = "không có";

    private final MutableLiveData<AccountInfoUiState> uiState =
            new MutableLiveData<>(AccountInfoUiState.empty());
    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    public AccountInfoViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<AccountInfoUiState> getUiState() {
        return uiState;
    }

    public void loadAccountInfo() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            uiState.setValue(AccountInfoUiState.empty());
            return;
        }

        userRepository.getUserById("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                List<User> users = response.body();
                if (!response.isSuccessful() || users == null || users.isEmpty()) {
                    uiState.setValue(AccountInfoUiState.empty());
                    return;
                }

                uiState.setValue(AccountInfoUiState.fromUser(users.get(0)));
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable throwable) {
                uiState.setValue(AccountInfoUiState.empty());
            }
        });
    }

    public static class AccountInfoUiState {
        public final String name;
        public final String phoneNumber;
        public final String email;
        public final String birthDate;
        public final String country;

        private AccountInfoUiState(String name, String phoneNumber, String email, String birthDate, String country) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.birthDate = birthDate;
            this.country = country;
        }

        public static AccountInfoUiState empty() {
            return new AccountInfoUiState(EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE);
        }

        public static AccountInfoUiState fromUser(User user) {
            String displayName = firstNonEmpty(user.getFullName(), user.getUsername());
            return new AccountInfoUiState(
                    valueOrEmpty(displayName),
                    valueOrEmpty(user.getPhoneNumber()),
                    valueOrEmpty(user.getEmail()),
                    valueOrEmpty(user.getBirthDate()),
                    valueOrEmpty(user.getCountry())
            );
        }

        private static String firstNonEmpty(String primary, String secondary) {
            if (primary != null && !primary.trim().isEmpty()) {
                return primary;
            }
            return secondary;
        }

        private static String valueOrEmpty(String value) {
            return value == null || value.trim().isEmpty() ? EMPTY_VALUE : value.trim();
        }
    }
}
