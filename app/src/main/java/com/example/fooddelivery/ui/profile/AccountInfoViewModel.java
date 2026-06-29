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
    private final MutableLiveData<UpdateResult> updateResult = new MutableLiveData<>();
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private boolean updating;

    public enum AccountField {
        NAME, PHONE, EMAIL, BIRTH_DATE, COUNTRY
    }

    public AccountInfoViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<AccountInfoUiState> getUiState() {
        return uiState;
    }

    public LiveData<UpdateResult> getUpdateResult() {
        return updateResult;
    }

    public void clearUpdateResult() {
        updateResult.setValue(null);
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

    public void updateField(AccountField field, String value) {
        int userId = sessionManager.getUserId();
        String normalized = value == null ? "" : value.trim();
        if (userId <= 0 || normalized.isEmpty() || updating) {
            return;
        }

        User patch = createPatch(field, normalized);
        updating = true;
        userRepository.updateUser("eq." + userId, patch).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                updating = false;
                if (!response.isSuccessful()) {
                    updateResult.setValue(new UpdateResult(false, field));
                    return;
                }
                AccountInfoUiState current = uiState.getValue();
                uiState.setValue((current == null ? AccountInfoUiState.empty() : current)
                        .withValue(field, normalized));
                updateResult.setValue(new UpdateResult(true, field));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                updating = false;
                updateResult.setValue(new UpdateResult(false, field));
            }
        });
    }

    static User createPatch(AccountField field, String value) {
        User patch = new User();
        switch (field) {
            case NAME:
                patch.setFullName(value);
                break;
            case PHONE:
                patch.setPhoneNumber(value);
                break;
            case EMAIL:
                patch.setEmail(value);
                break;
            case BIRTH_DATE:
                patch.setBirthDate(value);
                break;
            case COUNTRY:
                patch.setCountry(value);
                break;
        }
        return patch;
    }

    public static class UpdateResult {
        public final boolean successful;
        public final AccountField field;

        UpdateResult(boolean successful, AccountField field) {
            this.successful = successful;
            this.field = field;
        }
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
                    displayBirthDate(user.getBirthDate()),
                    valueOrEmpty(user.getCountry())
            );
        }

        AccountInfoUiState withValue(AccountField field, String value) {
            switch (field) {
                case NAME:
                    return new AccountInfoUiState(value, phoneNumber, email, birthDate, country);
                case PHONE:
                    return new AccountInfoUiState(name, value, email, birthDate, country);
                case EMAIL:
                    return new AccountInfoUiState(name, phoneNumber, value, birthDate, country);
                case BIRTH_DATE:
                    return new AccountInfoUiState(name, phoneNumber, email,
                            AccountInfoValidator.formatBirthDateForDisplay(value), country);
                case COUNTRY:
                    return new AccountInfoUiState(name, phoneNumber, email, birthDate, value);
                default:
                    return this;
            }
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

        private static String displayBirthDate(String value) {
            String normalized = valueOrEmpty(value);
            return EMPTY_VALUE.equals(normalized)
                    ? EMPTY_VALUE
                    : AccountInfoValidator.formatBirthDateForDisplay(normalized);
        }
    }
}
