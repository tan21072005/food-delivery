package com.example.fooddelivery.ui.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SignupRequiresLoginRegressionTest {

    @Test
    public void successfulSignupReturnsToLoginInsteadOfOpeningMain() throws Exception {
        String signUpFragment = readFile("src/main/java/com/example/fooddelivery/ui/auth/SignUpFragment.java");

        assertTrue(signUpFragment.contains("Navigation.findNavController(requireView()).popBackStack()"));
        assertFalse(signUpFragment.contains("new Intent(requireActivity(), MainActivity.class)"));
        assertFalse(signUpFragment.contains("goToMain()"));
    }

    @Test
    public void signupDoesNotSaveAnAuthenticatedSessionEvenIfSupabaseReturnsOne() throws Exception {
        String authViewModel = readFile("src/main/java/com/example/fooddelivery/ui/auth/AuthViewModel.java");
        String signUpMethod = authViewModel.substring(
                authViewModel.indexOf("public void signUp"),
                authViewModel.indexOf("private void completeAuthenticatedSession")
        );

        assertTrue(signUpMethod.contains("signupSuccess.setValue(true)"));
        assertFalse(signUpMethod.contains("completeAuthenticatedSession"));
        assertFalse(signUpMethod.contains("sessionManager.saveSession"));
    }

    private String readFile(String path) throws Exception {
        Path moduleRelative = Paths.get(path);
        Path sourcePath = Files.exists(moduleRelative) ? moduleRelative : Paths.get("app").resolve(path);
        return new String(Files.readAllBytes(sourcePath), java.nio.charset.StandardCharsets.UTF_8);
    }
}
