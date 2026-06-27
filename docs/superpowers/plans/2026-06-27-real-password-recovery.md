# Real Password Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the simulated forgot-password screens with a real six-digit email OTP recovery flow backed by Supabase Auth.

**Architecture:** Add a recovery-only Retrofit client so existing authenticated networking remains untouched. Three existing Fragments share an `AuthActivity`-scoped `PasswordRecoveryViewModel`; the ViewModel owns ephemeral email/token/cooldown state and delegates network work to `PasswordRecoveryRepository`.

**Tech Stack:** Java 11, Android Fragments, Android Lifecycle ViewModel/LiveData, Retrofit 2.9, OkHttp 4.12, Gson, Supabase Auth REST API, JUnit 4, MockWebServer.

## Global Constraints

- Modify only Auth/recovery source, its three layouts, additive string resources, test dependencies, and recovery tests.
- Do not change login, signup, profile, cart, order, menu, or shared authenticated-network behavior.
- Use email OTP with `/auth/v1/recover`, `/auth/v1/verify` type `recovery`, and `/auth/v1/user`.
- Keep the recovery access token only in `PasswordRecoveryViewModel` memory; never write it to `SessionManager`, Bundle, preferences, or logs.
- Keep `MODE_CREATE` and `MODE_CHANGE` behavior unchanged.
- Do not use RecyclerView for fixed forms.

---

### Task 1: Recovery API contract and isolated client

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/data/remote/request/PasswordRecoveryRequests.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/remote/apis/PasswordRecoveryApiService.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/remote/PasswordRecoveryApiClient.java`
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/java/com/example/fooddelivery/PasswordRecoveryApiContractTest.java`

**Interfaces:**
- Produces: `PasswordRecoveryRequests.Email`, `.VerifyOtp`, `.NewPassword`.
- Produces: `PasswordRecoveryApiService.sendCode`, `verifyOtp`, and `updatePassword`.
- Produces: `PasswordRecoveryApiClient.create()` returning the isolated service.

- [ ] **Step 1: Add test-only HTTP support**

Add to `dependencies`:

```kotlin
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")
```

- [ ] **Step 2: Write failing API contract tests**

Create reflection tests that assert:

```java
assertEquals("auth/v1/recover",
        PasswordRecoveryApiService.class.getMethod(
                "sendCode", String.class, PasswordRecoveryRequests.Email.class)
                .getAnnotation(POST.class).value());
assertEquals("auth/v1/verify",
        PasswordRecoveryApiService.class.getMethod(
                "verifyOtp", String.class, PasswordRecoveryRequests.VerifyOtp.class)
                .getAnnotation(POST.class).value());
assertEquals("auth/v1/user",
        PasswordRecoveryApiService.class.getMethod(
                "updatePassword", String.class, PasswordRecoveryRequests.NewPassword.class)
                .getAnnotation(PUT.class).value());
```

Also serialize requests with Gson and assert:

```java
assertEquals("{\"email\":\"user@example.com\"}",
        gson.toJson(new PasswordRecoveryRequests.Email("user@example.com")));
assertEquals("{\"email\":\"user@example.com\",\"token\":\"123456\",\"type\":\"recovery\"}",
        gson.toJson(new PasswordRecoveryRequests.VerifyOtp("user@example.com", "123456")));
assertEquals("{\"password\":\"NewPassword1!\"}",
        gson.toJson(new PasswordRecoveryRequests.NewPassword("NewPassword1!")));
```

- [ ] **Step 3: Run tests and confirm compilation failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.PasswordRecoveryApiContractTest"
```

Expected: FAIL because the recovery classes do not exist.

- [ ] **Step 4: Implement request models and service**

`PasswordRecoveryRequests` is a non-instantiable holder with immutable nested request classes and `@SerializedName` fields.

Define:

```java
@POST("auth/v1/recover")
Call<Void> sendCode(
        @Header("Authorization") String authorization,
        @Body PasswordRecoveryRequests.Email request);

@POST("auth/v1/verify")
Call<AuthResponse> verifyOtp(
        @Header("Authorization") String authorization,
        @Body PasswordRecoveryRequests.VerifyOtp request);

@PUT("auth/v1/user")
Call<ResponseBody> updatePassword(
        @Header("Authorization") String authorization,
        @Body PasswordRecoveryRequests.NewPassword request);
```

`PasswordRecoveryApiClient.create()` builds a separate Retrofit instance using `Constants.SUPABASE_URL`. Its interceptor uses `header`, not `addHeader`, for:

```java
request.newBuilder()
        .header("apikey", Constants.SUPABASE_ANON_KEY)
        .header("Content-Type", "application/json")
        .build();
```

It does not read `SessionManager`.

- [ ] **Step 5: Run API contract tests**

Run the Task 1 test command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```powershell
git add app/build.gradle.kts app/src/main/java/com/example/fooddelivery/data/remote/request/PasswordRecoveryRequests.java app/src/main/java/com/example/fooddelivery/data/remote/apis/PasswordRecoveryApiService.java app/src/main/java/com/example/fooddelivery/data/remote/PasswordRecoveryApiClient.java app/src/test/java/com/example/fooddelivery/PasswordRecoveryApiContractTest.java
git commit -m "feat: add password recovery API contract"
```

### Task 2: Recovery repository and error mapping

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepository.java`
- Create: `app/src/test/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepositoryTest.java`

**Interfaces:**
- Consumes: `PasswordRecoveryApiService`.
- Produces:

```java
interface ResultCallback<T> {
    void onSuccess(T value);
    void onError(RecoveryError error);
}

final class RecoveryError {
    int statusCode();
    String userMessage();
}

void sendCode(String email, ResultCallback<Void> callback);
void verifyOtp(String email, String otp, ResultCallback<String> callback);
void updatePassword(String recoveryToken, String password, ResultCallback<Void> callback);
```

- [ ] **Step 1: Write failing MockWebServer tests**

Test exact requests:

```java
repository.sendCode("user@example.com", callback);
assertEquals("/auth/v1/recover", server.takeRequest().getPath());

repository.verifyOtp("user@example.com", "123456", callback);
assertTrue(server.takeRequest().getBody().readUtf8().contains("\"type\":\"recovery\""));

repository.updatePassword("recovery-token", "NewPassword1!", callback);
RecordedRequest request = server.takeRequest();
assertEquals("Bearer recovery-token", request.getHeader("Authorization"));
```

Enqueue HTTP 403 and 429 responses and assert friendly messages for expired/invalid OTP and rate limiting. Enqueue verify HTTP 200 without `access_token` and assert an error rather than success.

- [ ] **Step 2: Run repository tests and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.PasswordRecoveryRepositoryTest"
```

Expected: FAIL because the repository is absent.

- [ ] **Step 3: Implement repository**

Provide a production constructor using `PasswordRecoveryApiClient.create()` and a public constructor accepting `PasswordRecoveryApiService` for tests. Define `ResultCallback` and immutable `RecoveryError` as public nested types of `PasswordRecoveryRepository`. Use:

```java
private String anonBearer() {
    return "Bearer " + Constants.SUPABASE_ANON_KEY;
}
```

Map statuses:

```java
403 -> "Mã xác minh không đúng hoặc đã hết hạn"
429 -> "Bạn thao tác quá nhanh. Vui lòng thử lại sau"
other non-2xx -> "Không thể xử lý yêu cầu. Vui lòng thử lại"
network failure -> "Không thể kết nối. Vui lòng kiểm tra mạng"
```

Return `AuthResponse.accessToken` from successful verification and reject a blank token.

- [ ] **Step 4: Run repository tests**

Run the Task 2 command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepository.java app/src/test/java/com/example/fooddelivery/data/repository/PasswordRecoveryRepositoryTest.java
git commit -m "feat: add password recovery repository"
```

### Task 3: Recovery ViewModel, events, validation, and cooldown

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/ui/auth/RecoveryEvent.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryValidator.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModel.java`
- Create: `app/src/test/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModelTest.java`

**Interfaces:**
- Consumes: `PasswordRecoveryRepository`.
- Produces:

```java
LiveData<Boolean> getLoading();
LiveData<RecoveryEvent> getEvents();
String getEmail();
String getMaskedEmail();
boolean hasVerifiedRecovery();
long getResendSecondsRemaining();
void requestCode(String email);
void resendCode();
void verifyOtp(String otp);
void updatePassword(String password, String confirmation);
void restart();
```

`RecoveryEvent.Type` contains `CODE_SENT`, `CODE_RESENT`, `OTP_VERIFIED`, `PASSWORD_UPDATED`, and `ERROR`.

- [ ] **Step 1: Write failing validator/ViewModel tests**

Use `InstantTaskExecutorRule` and a fake repository. Assert:

```java
assertFalse(PasswordRecoveryValidator.isValidEmail("bad"));
assertFalse(PasswordRecoveryValidator.isValidOtp("12345"));
assertTrue(PasswordRecoveryValidator.isValidOtp("123456"));
assertFalse(PasswordRecoveryValidator.isStrongPassword("password"));
assertTrue(PasswordRecoveryValidator.isStrongPassword("NewPassword1!"));
```

Then assert:

- `requestCode` normalizes email and emits `CODE_SENT` only after repository success.
- `verifyOtp` stores only a successful nonblank token.
- `updatePassword` rejects mismatch before repository invocation.
- update success clears email and token and emits `PASSWORD_UPDATED`.
- resend is blocked until 60 seconds have elapsed.
- `restart()` clears all sensitive state.
- `RecoveryEvent.consume()` returns content once and then returns null.

- [ ] **Step 2: Run ViewModel tests and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.auth.PasswordRecoveryViewModelTest"
```

Expected: FAIL because the ViewModel classes are absent.

- [ ] **Step 3: Implement validation and one-shot events**

Validation rules:

```java
EMAIL_ADDRESS.matcher(email).matches()
otp.matches("\\d{6}")
password.length() >= 8 && password.length() <= 20
password.matches(".*[A-Za-z].*")
password.matches(".*\\d.*")
password.matches(".*[^A-Za-z0-9].*")
```

`RecoveryEvent.consume()` synchronizes access to a private `handled` flag.

- [ ] **Step 4: Implement ViewModel**

Use an injected `LongSupplier clockMillis` in the test constructor and `System::currentTimeMillis` in production. Set `resendAvailableAt = clock + 60_000L` after a successful send/resend. Set loading before repository calls and clear it in every callback. Never expose the recovery token through LiveData or getters.

- [ ] **Step 5: Run ViewModel tests**

Run the Task 3 command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/auth/RecoveryEvent.java app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryValidator.java app/src/main/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModel.java app/src/test/java/com/example/fooddelivery/ui/auth/PasswordRecoveryViewModelTest.java
git commit -m "feat: add password recovery view model"
```

### Task 4: Connect the email request Fragment

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/ForgetPassFragment.java`
- Modify: `app/src/main/res/layout/auth_fragment_forget_password.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Create: `app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java`

**Interfaces:**
- Consumes: shared `PasswordRecoveryViewModel`.
- Produces: real `requestCode(email)` behavior and navigation only on `CODE_SENT`.

- [ ] **Step 1: Write failing source/layout contract tests**

Assert that `ForgetPassFragment` contains:

```java
new ViewModelProvider(requireActivity()).get(PasswordRecoveryViewModel.class)
passwordRecoveryViewModel.requestCode(email)
R.id.action_forget_to_otp
```

Parse the layout and assert `edEmailPhone` has `android:inputType="textEmailAddress"` and uses email-only hint/note string resources.

- [ ] **Step 2: Run contract test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.PasswordRecoveryFragmentContractTest"
```

Expected: FAIL because the Fragment still navigates locally.

- [ ] **Step 3: Implement the email screen**

Obtain the shared ViewModel, observe loading and one-shot events, disable `edEmailPhone` and `btnNext` while loading, show validation errors, call `requestCode`, and navigate only for `CODE_SENT`. Keep `tvBack` behavior and call `restart()` before leaving.

Replace hard-coded UI text with additive localized recovery strings; do not edit unrelated strings.

- [ ] **Step 4: Run the contract test**

Run the Task 4 command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/auth/ForgetPassFragment.java app/src/main/res/layout/auth_fragment_forget_password.xml app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java
git commit -m "feat: send password recovery email"
```

### Task 5: Connect OTP verification and resend

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/VerifyOtpFragment.java`
- Modify: `app/src/main/res/layout/auth_fragment_verify_otp.xml`
- Modify: `app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java`

**Interfaces:**
- Consumes: ViewModel email, masked email, cooldown, `verifyOtp`, and `resendCode`.
- Produces: navigation to reset only on `OTP_VERIFIED`.

- [ ] **Step 1: Extend the failing contract test**

Assert the source no longer contains `CORRECT_OTP` and does contain:

```java
passwordRecoveryViewModel.verifyOtp(otp)
passwordRecoveryViewModel.resendCode()
passwordRecoveryViewModel.getMaskedEmail()
R.id.action_otp_to_reset
```

- [ ] **Step 2: Run and verify the test fails**

Run the Task 4 command. Expected: FAIL on hard-coded OTP behavior.

- [ ] **Step 3: Implement OTP screen**

If ViewModel email is blank, pop back to email entry. Bind masked email, validate six digits, call real verify/resend methods, disable controls while loading, and update `tvResendCode` each second with the remaining cooldown. Cancel the screen timer in `onDestroyView`. On change-email, call `restart()` and pop back. Navigate only for `OTP_VERIFIED`.

- [ ] **Step 4: Run contract and ViewModel tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.PasswordRecoveryFragmentContractTest" --tests "com.example.fooddelivery.ui.auth.PasswordRecoveryViewModelTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/auth/VerifyOtpFragment.java app/src/main/res/layout/auth_fragment_verify_otp.xml app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java
git commit -m "feat: verify and resend recovery OTP"
```

### Task 6: Connect password update and eye toggles

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/auth/PasswordFormFragment.java`
- Modify: `app/src/main/res/layout/fragment_password_form.xml`
- Modify: `app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java`

**Interfaces:**
- Consumes: `hasVerifiedRecovery()` and `updatePassword`.
- Produces: real update in `MODE_RESET`, while preserving existing create/change branches.

- [ ] **Step 1: Extend failing contract tests**

Assert source contains:

```java
if (MODE_RESET.equals(currentMode))
passwordRecoveryViewModel.updatePassword(newPass, confirmPass)
R.id.action_reset_to_login
PasswordTransformationMethod.getInstance()
HideReturnsTransformationMethod.getInstance()
```

Assert reset success does not execute the generic fake-success Toast/pop-back path.

- [ ] **Step 2: Run and verify test failure**

Run the Task 4 command. Expected: FAIL because reset still uses fake success.

- [ ] **Step 3: Implement reset-only update behavior**

For `MODE_RESET`, require a verified recovery token, delegate all password validation/update to the recovery ViewModel, observe loading/events, and navigate with `action_reset_to_login` only on `PASSWORD_UPDATED`. Keep the current local behavior of `MODE_CREATE` and `MODE_CHANGE` byte-for-byte except for extracting shared view setup.

Attach click listeners to `ivToggleNew` and `ivToggleConfirm`. Toggle transformations and drawable `ic_eye`/`ic_eye_off`, then restore `editText.setSelection(editText.length())`.

Update the reset-mode rule text to 8–20 characters with letter, number, and special-character requirements.

- [ ] **Step 4: Run recovery tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.PasswordRecoveryFragmentContractTest" --tests "com.example.fooddelivery.ui.auth.PasswordRecoveryViewModelTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/auth/PasswordFormFragment.java app/src/main/res/layout/fragment_password_form.xml app/src/test/java/com/example/fooddelivery/PasswordRecoveryFragmentContractTest.java
git commit -m "feat: update password with recovery token"
```

### Task 7: Full regression and build verification

**Files:**
- Modify only if verification exposes a recovery-scope defect.

**Interfaces:**
- Verifies all previous tasks together.

- [ ] **Step 1: Run all unit tests**

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Build the debug APK**

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Verify scope**

Run:

```powershell
git diff --name-only 2094d7e..HEAD
```

Expected: only the files listed in Tasks 1–6 plus this plan.

- [ ] **Step 4: Complete the external configuration checklist**

In Supabase Dashboard:

1. Open Authentication → Email Templates → Reset Password.
2. Ensure the body displays `{{ .Token }}`.
3. Save the template.
4. Use a test account to verify send, invalid OTP, valid OTP, update, old-password rejection, new-password login, and 60-second resend behavior.

- [ ] **Step 5: Record results**

Report unit-test result, APK build result, manual Supabase checks completed or still requiring user access, and any unchanged unrelated working-tree files.
