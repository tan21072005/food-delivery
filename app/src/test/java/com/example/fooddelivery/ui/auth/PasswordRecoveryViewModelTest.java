package com.example.fooddelivery.ui.auth;

import android.app.Application;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.fooddelivery.data.repository.PasswordRecoveryRepository;

import org.junit.Rule;
import org.junit.Test;

import java.util.function.LongSupplier;

public class PasswordRecoveryViewModelTest {
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void validatesRecoveryInputs() {
        assertFalse(PasswordRecoveryValidator.isValidEmail("bad"));
        assertTrue(PasswordRecoveryValidator.isValidEmail("user@example.com"));
        assertFalse(PasswordRecoveryValidator.isValidEmail("user@gmail.co"));
        assertFalse(PasswordRecoveryValidator.isValidOtp("12345"));
        assertTrue(PasswordRecoveryValidator.isValidOtp("123456"));
        assertFalse(PasswordRecoveryValidator.isStrongPassword("password"));
        assertTrue(PasswordRecoveryValidator.isStrongPassword("NewPassword1!"));
    }

    @Test
    public void eventCanOnlyBeConsumedOnce() {
        RecoveryEvent event = new RecoveryEvent(RecoveryEvent.Type.CODE_SENT, null);
        assertSame(event, event.consume());
        assertNull(event.consume());
    }

    @Test
    public void suggestsCorrectionsForCommonGmailDomainTypos() {
        assertEquals("user@gmail.com",
                PasswordRecoveryValidator.suggestEmailCorrection("user@gmail.co"));
        assertEquals("user@gmail.com",
                PasswordRecoveryValidator.suggestEmailCorrection("user@gmai.com"));
        assertEquals("user@gmail.com",
                PasswordRecoveryValidator.suggestEmailCorrection("user@gmial.com"));
        assertNull(PasswordRecoveryValidator.suggestEmailCorrection("user@yahoo.co"));
    }

    @Test
    public void gmailDomainTypoDoesNotStartRecoveryRequest() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());

        viewModel.requestCode("user@gmail.co");

        assertEquals(0, repository.sendCodeCount);
    }

    @Test
    public void ignoresDuplicateRecoveryRequestsWhileOneIsInFlight() {
        FakeRepository repository = new FakeRepository();
        MutableClock clock = new MutableClock();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, clock);

        viewModel.requestCode("user@example.com");
        viewModel.requestCode("user@example.com");

        assertEquals(1, repository.sendCodeCount);
    }

    @Test
    public void restartInvalidatesAnOlderNetworkCallback() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());

        viewModel.requestCode("user@example.com");
        viewModel.restart();
        repository.sendCodeCallback.onSuccess(null);

        assertNull(viewModel.getEmail());
        assertNull(viewModel.getEvents().getValue());
        assertFalse(Boolean.TRUE.equals(viewModel.getLoading().getValue()));
    }

    @Test
    public void restartClearsAnUnconsumedEvent() {
        PasswordRecoveryViewModel viewModel = new PasswordRecoveryViewModel(
                new Application(), new FakeRepository(), new MutableClock());
        viewModel.requestCode("invalid-email");
        assertNotNull(viewModel.getEvents().getValue());

        viewModel.restart();

        assertNull(viewModel.getEvents().getValue());
    }

    @Test
    public void requestingCodeForDifferentEmailClearsVerifiedRecoveryState() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());

        viewModel.requestCode("first@example.com");
        repository.sendCodeCallback.onSuccess(null);
        viewModel.verifyOtp("123456");
        repository.verifyOtpCallback.onSuccess("first-recovery-token");
        assertTrue(viewModel.hasVerifiedRecovery());

        viewModel.requestCode("second@example.com");

        assertEquals("second@example.com", viewModel.getEmail());
        assertFalse(viewModel.hasVerifiedRecovery());
        assertEquals(0, viewModel.getResendSecondsRemaining());
    }

    @Test
    public void rateLimitResponseStartsLocalCooldown() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());

        viewModel.requestCode("user@example.com");
        repository.sendCodeCallback.onError(
                new PasswordRecoveryRepository.RecoveryError(429, "rate limited"));

        assertEquals(60, viewModel.getResendSecondsRemaining());
        viewModel.requestCode("user@example.com");
        assertEquals(1, repository.sendCodeCount);
    }

    @Test
    public void ignoresDuplicateOtpVerificationWhileOneIsInFlight() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());
        viewModel.requestCode("user@example.com");
        repository.sendCodeCallback.onSuccess(null);

        viewModel.verifyOtp("123456");
        viewModel.verifyOtp("123456");

        assertEquals(1, repository.verifyOtpCount);
    }

    @Test
    public void ignoresDuplicateResendWhileOneIsInFlight() {
        FakeRepository repository = new FakeRepository();
        MutableClock clock = new MutableClock();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, clock);
        viewModel.requestCode("user@example.com");
        repository.sendCodeCallback.onSuccess(null);
        clock.now = 60_000L;

        viewModel.resendCode();
        viewModel.resendCode();

        assertEquals(2, repository.sendCodeCount);
    }

    @Test
    public void expiredRecoveryTokenClearsStateAndRequestsRestart() {
        FakeRepository repository = new FakeRepository();
        PasswordRecoveryViewModel viewModel =
                new PasswordRecoveryViewModel(new Application(), repository, new MutableClock());
        viewModel.requestCode("user@example.com");
        repository.sendCodeCallback.onSuccess(null);
        viewModel.verifyOtp("123456");
        repository.verifyOtpCallback.onSuccess("recovery-token");

        viewModel.updatePassword("NewPassword1!", "NewPassword1!");
        repository.updatePasswordCallback.onError(
                new PasswordRecoveryRepository.RecoveryError(401, "expired"));

        RecoveryEvent event = viewModel.getEvents().getValue();
        assertNotNull(event);
        assertEquals(RecoveryEvent.Type.RECOVERY_EXPIRED, event.type());
        assertNull(viewModel.getEmail());
        assertFalse(viewModel.hasVerifiedRecovery());
    }

    private static final class MutableClock implements LongSupplier {
        private long now;

        @Override public long getAsLong() {
            return now;
        }
    }

    private static final class FakeRepository extends PasswordRecoveryRepository {
        private int sendCodeCount;
        private ResultCallback<Void> sendCodeCallback;
        private int verifyOtpCount;
        private ResultCallback<String> verifyOtpCallback;
        private ResultCallback<Void> updatePasswordCallback;

        FakeRepository() {
            super(null, "test-anon-key");
        }

        @Override
        public void sendCode(String email, ResultCallback<Void> callback) {
            sendCodeCount++;
            sendCodeCallback = callback;
        }

        @Override
        public void verifyOtp(String email, String otp, ResultCallback<String> callback) {
            verifyOtpCount++;
            verifyOtpCallback = callback;
        }

        @Override
        public void updatePassword(String recoveryToken, String password,
                                   ResultCallback<Void> callback) {
            updatePasswordCallback = callback;
        }
    }
}
