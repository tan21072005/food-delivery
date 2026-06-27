# Reset Password Confirm Eye Icon Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the heart icons beside both reset-password fields with the existing hidden-eye icon.

**Architecture:** Keep the shared password form unchanged except for the `android:src` of `ivToggleNew` and `ivToggleConfirm`. Use a focused XML regression test that identifies both views by ID and asserts their drawables.

**Tech Stack:** Android XML resources, Java 11, JUnit 4, Gradle.

## Global Constraints

- Change `ivToggleNew` and `ivToggleConfirm` from `@drawable/ic_favorite_border` to `@drawable/ic_eye_off`.
- Keep its dimensions, positioning, content description, and current behavior unchanged.
- Do not change `ivToggleOld`.
- Do not add password visibility-toggle behavior.

---

### Task 1: Correct both reset-password icons

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`
- Modify: `app/src/main/res/layout/fragment_password_form.xml`

**Interfaces:**
- Consumes: Android XML `android:id` and `android:src` attributes.
- Produces: `ivToggleNew` and `ivToggleConfirm` rendering `@drawable/ic_eye_off`.

- [x] **Step 1: Write the failing regression test**

Add this test to `BugRegressionTest`:

```java
@Test
public void resetPasswordConfirmationUsesEyeIcon() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document document = factory.newDocumentBuilder().parse(
            Files.newInputStream(projectPath("src/main/res/layout/fragment_password_form.xml")));

    NodeList imageViews = document.getElementsByTagName("ImageView");
    for (int i = 0; i < imageViews.getLength(); i++) {
        org.w3c.dom.Element imageView = (org.w3c.dom.Element) imageViews.item(i);
        String id = imageView.getAttributeNS(
                "http://schemas.android.com/apk/res/android", "id");
        if ("@+id/ivToggleConfirm".equals(id)) {
            assertEquals("@drawable/ic_eye_off", imageView.getAttributeNS(
                    "http://schemas.android.com/apk/res/android", "src"));
            return;
        }
    }
    throw new AssertionError("Missing ivToggleConfirm");
}
```

- [x] **Step 2: Run the test and verify the current heart icon fails it**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.BugRegressionTest.resetPasswordConfirmationUsesEyeIcon"
```

Expected: `FAILED`, showing expected `@drawable/ic_eye_off` but found `@drawable/ic_favorite_border`.

- [x] **Step 3: Replace only the confirmation field drawable**

In the `ivToggleConfirm` element, change:

```xml
android:src="@drawable/ic_favorite_border"
```

to:

```xml
android:src="@drawable/ic_eye_off"
```

- [x] **Step 4: Run verification**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.BugRegressionTest.resetPasswordConfirmationUsesEyeIcon"
.\gradlew.bat :app:testDebugUnitTest
```

Expected: both commands finish with `BUILD SUCCESSFUL`.

- [x] **Step 5: Commit the implementation**

```powershell
git add app/src/test/java/com/example/fooddelivery/BugRegressionTest.java app/src/main/res/layout/fragment_password_form.xml docs/superpowers/plans/2026-06-27-reset-password-confirm-eye-icon.md
git commit -m "fix: use eye icon for password confirmation"
```

### Task 2: Correct the new-password icon

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`
- Modify: `app/src/main/res/layout/fragment_password_form.xml`

**Interfaces:**
- Consumes: Android XML `android:id` and `android:src` attributes.
- Produces: `ivToggleNew` rendering `@drawable/ic_eye_off`.

- [ ] **Step 1: Write the failing regression test**

Add:

```java
@Test
public void resetPasswordNewFieldUsesEyeIcon() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document document = factory.newDocumentBuilder().parse(
            Files.newInputStream(projectPath("src/main/res/layout/fragment_password_form.xml")));

    NodeList imageViews = document.getElementsByTagName("ImageView");
    for (int i = 0; i < imageViews.getLength(); i++) {
        org.w3c.dom.Element imageView = (org.w3c.dom.Element) imageViews.item(i);
        String id = imageView.getAttributeNS(
                "http://schemas.android.com/apk/res/android", "id");
        if ("@+id/ivToggleNew".equals(id)) {
            assertEquals("@drawable/ic_eye_off", imageView.getAttributeNS(
                    "http://schemas.android.com/apk/res/android", "src"));
            return;
        }
    }
    throw new AssertionError("Missing ivToggleNew");
}
```

- [ ] **Step 2: Verify the current heart icon fails the test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.BugRegressionTest.resetPasswordNewFieldUsesEyeIcon"
```

Expected: `FAILED`, showing expected `@drawable/ic_eye_off` but found `@drawable/ic_favorite_border`.

- [ ] **Step 3: Replace only the new-password drawable**

In the `ivToggleNew` element, change:

```xml
android:src="@drawable/ic_favorite_border"
```

to:

```xml
android:src="@drawable/ic_eye_off"
```

- [ ] **Step 4: Run verification**

Run the targeted test and then `.\gradlew.bat :app:testDebugUnitTest`. Both commands must finish with `BUILD SUCCESSFUL`.
