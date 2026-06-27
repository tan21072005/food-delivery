package com.example.fooddelivery.ui.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordRecoveryViewModelTest {
    @Test
    public void validatesRecoveryInputs() {
        assertFalse(PasswordRecoveryValidator.isValidEmail("bad"));
        assertTrue(PasswordRecoveryValidator.isValidEmail("user@example.com"));
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
}
