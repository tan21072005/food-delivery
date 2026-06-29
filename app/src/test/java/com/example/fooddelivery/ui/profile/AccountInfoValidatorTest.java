package com.example.fooddelivery.ui.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.model.User;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountInfoValidatorTest {

    @Test
    public void validatesEmailAndPhoneFormats() {
        assertTrue(AccountInfoValidator.isValidEmail("student@example.com"));
        assertFalse(AccountInfoValidator.isValidEmail("student@"));
        assertTrue(AccountInfoValidator.isValidPhone("+84941189263"));
        assertFalse(AccountInfoValidator.isValidPhone("12-ab"));
    }

    @Test
    public void validatesAndFormatsBirthDate() throws Exception {
        Date today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("2026-06-29");
        assertTrue(AccountInfoValidator.isValidBirthDate("2005-07-21", today));
        assertFalse(AccountInfoValidator.isValidBirthDate("2027-01-01", today));
        assertFalse(AccountInfoValidator.isValidBirthDate("21/07/2005", today));
        assertEquals("21/07/2005", AccountInfoValidator.formatBirthDateForDisplay("2005-07-21"));
    }

    @Test
    public void createsSingleFieldPatch() {
        User patch = AccountInfoViewModel.createPatch(
                AccountInfoViewModel.AccountField.EMAIL, "student@example.com");

        assertEquals("student@example.com", patch.getEmail());
        assertNull(patch.getFullName());
        assertNull(patch.getPhoneNumber());
        assertNull(patch.getBirthDate());
        assertNull(patch.getCountry());
    }
}
