# Password Recovery Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the reviewed gaps between the real password recovery implementation and its approved design.

**Architecture:** Keep the existing Fragment → activity-scoped ViewModel → Repository flow. Make the ViewModel the single owner of in-flight request sequencing, cooldown, and recovery-state invalidation; keep Supabase error parsing in the Repository and UI enablement/navigation in the Fragments.

**Tech Stack:** Java 11, Android Lifecycle, Retrofit, Gson, JUnit 4, MockWebServer.

## Global Constraints

- Keep the recovery token in ViewModel memory only.
- Do not change `MODE_CREATE` or `MODE_CHANGE`.
- Do not expose raw Supabase errors to UI.
- Preserve the existing three Supabase Auth REST endpoints and headers.

---

### Task 1: Reject duplicate and stale recovery operations

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModelTest.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModel.java`

**Interfaces:**
- Preserve the current public ViewModel interface.
- Produce deterministic single-operation behavior through that interface.

- [x] Add a controllable repository adapter and tests proving duplicate calls are ignored, stale callbacks cannot mutate state, a different email clears old recovery state, and HTTP 429 starts a 60-second cooldown.
- [x] Run the focused ViewModel test and confirm the new tests fail.
- [x] Add operation generation/in-flight state to the ViewModel and invalidate callbacks on restart or a new email.
- [x] Run the focused test and confirm it passes.

### Task 2: Recover safely from an expired recovery token

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModelTest.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/RecoveryEvent.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModel.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordFormFragment.java`

**Interfaces:**
- Add `RecoveryEvent.Type.RECOVERY_EXPIRED`.
- On update HTTP 401/403, clear recovery state and emit `RECOVERY_EXPIRED`.

- [x] Add a failing test for update HTTP 401/403 clearing state and emitting `RECOVERY_EXPIRED`.
- [x] Implement the event and navigate to `forgetPassFragment` from reset mode.
- [x] Run the focused test and confirm it passes.

### Task 3: Parse Supabase errors safely

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepositoryTest.java`
- Modify: `app/src/main/java/com/example/fooddelivery/data/remote/response/AuthError.java`
- Modify: `app/src/main/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepository.java`

**Interfaces:**
- Preserve `RecoveryError(statusCode, userMessage)`.
- Parse documented `error_description`, `msg`, and `message` fields only to classify known safe cases; retain HTTP-status fallback messages.

- [x] Add a failing MockWebServer test for parsed invalid/expired OTP and a fallback test that does not expose an unknown server message.
- [x] Correct the error model and parse `errorBody()` defensively.
- [x] Run the focused repository test and confirm it passes.

### Task 4: Complete loading and lifecycle UI behavior

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/VerifyOtpFragment.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordFormFragment.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/LoginFragment.java`

**Interfaces:**
- Disable resend and both reset-password visibility icons while loading.
- Clear recovery state whenever the login screen becomes active.

- [x] Bind all missing controls to loading state without changing create/change behavior.
- [x] Clear the activity-scoped recovery ViewModel in `LoginFragment`.
- [x] Run all unit tests and `:app:assembleDebug`.

### Task 5: Reject known Gmail domain typos at the validation seam

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModelTest.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryValidator.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/ForgetPassFragment.java`

**Interfaces:**
- `PasswordRecoveryValidator.isValidEmail(String)` rejects `gmail.co`, `gmai.com`, and `gmial.com`.
- `PasswordRecoveryValidator.suggestEmailCorrection(String)` continues returning the corresponding `gmail.com` correction.
- `ForgetPassFragment` shows the specific correction before the generic invalid-email error.

- [x] Add `assertFalse(PasswordRecoveryValidator.isValidEmail("user@gmail.co"))` and a ViewModel test proving no recovery request is issued for that address.
- [x] Run the focused test and confirm it fails because the syntax-only validator accepts `gmail.co`.
- [x] Make `isValidEmail` reject values for which `suggestEmailCorrection` returns a correction, and check the suggestion first in `ForgetPassFragment`.
- [x] Run the focused test, all unit tests, and `:app:assembleDebug`; expect `BUILD SUCCESSFUL`.
