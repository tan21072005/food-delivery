package com.example.fooddelivery.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.repository.PasswordRecoveryRepository;

import java.util.Locale;
import java.util.function.LongSupplier;

public class PasswordRecoveryViewModel extends AndroidViewModel {
    private final PasswordRecoveryRepository repository;
    private final LongSupplier clock;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<RecoveryEvent> events = new MutableLiveData<>();
    private String email;
    private String recoveryToken;
    private long resendAvailableAt;
    private boolean requestInFlight;
    private long operationGeneration;

    public PasswordRecoveryViewModel(@NonNull Application application) {
        this(application, new PasswordRecoveryRepository(), System::currentTimeMillis);
    }

    PasswordRecoveryViewModel(Application application, PasswordRecoveryRepository repository,
                              LongSupplier clock) {
        super(application);
        this.repository = repository;
        this.clock = clock;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<RecoveryEvent> getEvents() { return events; }
    public String getEmail() { return email; }
    public boolean hasVerifiedRecovery() { return recoveryToken != null; }

    public String getMaskedEmail() {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.substring(0, Math.min(2, at)) + "****" + email.substring(at);
    }

    public long getResendSecondsRemaining() {
        return Math.max(0, (resendAvailableAt - clock.getAsLong() + 999) / 1000);
    }

    public void requestCode(String input) {
        if (requestInFlight) return;
        String normalized = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        if (!PasswordRecoveryValidator.isValidEmail(normalized)) {
            emitError("Email không hợp lệ");
            return;
        }
        if (normalized.equals(email) && getResendSecondsRemaining() > 0) {
            emitError("Bạn thao tác quá nhanh. Vui lòng thử lại sau");
            return;
        }
        if (email != null && !email.equals(normalized)) {
            recoveryToken = null;
            resendAvailableAt = 0;
        }
        email = normalized;
        sendCode(RecoveryEvent.Type.CODE_SENT);
    }

    public void resendCode() {
        if (requestInFlight || email == null || getResendSecondsRemaining() > 0) return;
        sendCode(RecoveryEvent.Type.CODE_RESENT);
    }

    private void sendCode(RecoveryEvent.Type type) {
        requestInFlight = true;
        long operation = ++operationGeneration;
        loading.setValue(true);
        repository.sendCode(email, new PasswordRecoveryRepository.ResultCallback<Void>() {
            @Override public void onSuccess(Void value) {
                if (operation != operationGeneration) return;
                requestInFlight = false;
                resendAvailableAt = clock.getAsLong() + 60_000L;
                loading.setValue(false);
                events.setValue(new RecoveryEvent(type, null));
            }
            @Override public void onError(PasswordRecoveryRepository.RecoveryError error) {
                if (operation != operationGeneration) return;
                requestInFlight = false;
                if (error.statusCode() == 429) {
                    resendAvailableAt = clock.getAsLong() + 60_000L;
                }
                loading.setValue(false);
                emitError(error.userMessage());
            }
        });
    }

    public void verifyOtp(String otp) {
        if (requestInFlight) return;
        if (email == null || !PasswordRecoveryValidator.isValidOtp(otp)) {
            emitError("Mã xác minh phải gồm 6 chữ số");
            return;
        }
        requestInFlight = true;
        long operation = ++operationGeneration;
        loading.setValue(true);
        repository.verifyOtp(email, otp, new PasswordRecoveryRepository.ResultCallback<String>() {
            @Override public void onSuccess(String token) {
                if (operation != operationGeneration) return;
                requestInFlight = false;
                loading.setValue(false);
                recoveryToken = token;
                events.setValue(new RecoveryEvent(RecoveryEvent.Type.OTP_VERIFIED, null));
            }
            @Override public void onError(PasswordRecoveryRepository.RecoveryError error) {
                if (operation != operationGeneration) return;
                requestInFlight = false;
                loading.setValue(false);
                emitError(error.userMessage());
            }
        });
    }

    public void updatePassword(String password, String confirmation) {
        if (requestInFlight) return;
        if (recoveryToken == null) { emitError("Phiên khôi phục đã hết hạn"); return; }
        if (!PasswordRecoveryValidator.isStrongPassword(password)) {
            emitError("Mật khẩu phải dài 8–20 ký tự và có chữ, số, ký tự đặc biệt");
            return;
        }
        if (!password.equals(confirmation)) { emitError("Mật khẩu nhập lại không khớp"); return; }
        requestInFlight = true;
        long operation = ++operationGeneration;
        loading.setValue(true);
        repository.updatePassword(recoveryToken, password,
                new PasswordRecoveryRepository.ResultCallback<Void>() {
                    @Override public void onSuccess(Void value) {
                        if (operation != operationGeneration) return;
                        requestInFlight = false;
                        loading.setValue(false);
                        clear();
                        events.setValue(new RecoveryEvent(RecoveryEvent.Type.PASSWORD_UPDATED, null));
                    }
                    @Override public void onError(PasswordRecoveryRepository.RecoveryError error) {
                        if (operation != operationGeneration) return;
                        requestInFlight = false;
                        loading.setValue(false);
                        if (error.statusCode() == 401 || error.statusCode() == 403) {
                            clear();
                            events.setValue(new RecoveryEvent(
                                    RecoveryEvent.Type.RECOVERY_EXPIRED, error.userMessage()));
                            return;
                        }
                        emitError(error.userMessage());
                    }
                });
    }

    public void restart() {
        operationGeneration++;
        requestInFlight = false;
        loading.setValue(false);
        events.setValue(null);
        clear();
    }
    private void clear() { email = null; recoveryToken = null; resendAvailableAt = 0; }
    private void emitError(String message) {
        events.setValue(new RecoveryEvent(RecoveryEvent.Type.ERROR, message));
    }
}
