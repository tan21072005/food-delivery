# Reset Password Confirm Eye Icon Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the heart icon beside the reset-password confirmation field with the existing hidden-eye icon.

**Architecture:** Keep the shared password form unchanged except for the `android:src` of `ivToggleConfirm`. Add a focused XML regression test that identifies the view by ID and asserts its drawable, preventing accidental changes to neighboring password icons.

**Tech Stack:** Android XML resources, Java 11, JUnit 4, Gradle.

## Global Constraints

- Only `ivToggleConfirm` changes from `@drawable/ic_favorite_border` to `@drawable/ic_eye_off`.
- Keep its dimensions, positioning, content description, and current behavior unchanged.
- Do not change `ivToggleOld` or `ivToggleNew`.
- Do not add password visibility-toggle behavior.

---

### Task 1: Correct the confirmation password icon

**Files:**
- Modify: `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`
- Modify: `app/src/main/res/layout/fragment_password_form.xml`

**Interfaces:**
- Consumes: Android XML `android:id` and `android:src` attributes.
- Produces: `ivToggleConfirm` rendering `@drawable/ic_eye_off`.

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
