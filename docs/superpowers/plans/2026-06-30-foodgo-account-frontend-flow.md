# FoodGo Account Frontend Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng luồng khởi động FoodGo, chuyển đổi Việt–Anh trong Profile, nhận diện FG đồng nhất và giao diện phone/tablet portrait cho Splash, Login, Home và Profile.

**Architecture:** Một `SplashActivity` duy nhất quyết định đích đến từ `SessionManager`; quyết định routing được tách thành Java thuần để unit test. Locale dùng `AppCompatDelegate.setApplicationLocales`, còn UI dùng semantic resources và layout-width qualifiers để tablet vẫn là một cột căn giữa. Ba thành viên làm trên nhánh riêng và tích hợp tuần tự để tránh tranh chấp Manifest/resources.

**Tech Stack:** Android Java/XML Views, AppCompat, AndroidX SplashScreen 1.0.1, Navigation Component, Material Components, JUnit 4, Espresso.

## Global Constraints

- Nhánh đích là `dev`; nhánh tích hợp là `feature/foodgo-frontend`.
- Ngôn ngữ mặc định là Tiếng Việt; chỉ hỗ trợ `vi` và `en`.
- Thương hiệu là FoodGo; icon adaptive dùng monogram `FG`.
- Màu nhận diện là cam san hô và đỏ gạch.
- Tất cả Activity khóa portrait.
- Tablet 7–10 inch dùng bố cục một cột căn giữa với chiều rộng tối đa; không dùng master–detail.
- Không thay đổi nghiệp vụ đặt món, database, Supabase schema hoặc API ngoài việc đọc session hiện có.
- Không push hoặc merge vào `dev` khi chưa được người dùng cho phép.

---

## File Map

**Foundation — thành viên 1**

- Create `app/src/main/java/com/example/fooddelivery/ui/splash/LaunchDestination.java`: hai đích `AUTH` và `MAIN`.
- Create `app/src/main/java/com/example/fooddelivery/ui/splash/SessionDestinationResolver.java`: quyết định routing từ JWT cục bộ và thời gian hiện tại.
- Create `app/src/main/java/com/example/fooddelivery/ui/splash/SplashActivity.java`: launcher mỏng, không chứa nghiệp vụ session.
- Create `app/src/main/java/com/example/fooddelivery/data/local/prefs/LocaleStore.java`: chuẩn hóa và áp dụng `vi`/`en`.
- Create `app/src/test/java/com/example/fooddelivery/ui/splash/SessionDestinationResolverTest.java`.
- Create `app/src/test/java/com/example/fooddelivery/data/local/prefs/LocaleStoreTest.java`.
- Modify `app/src/main/java/com/example/fooddelivery/App.java`: áp dụng locale trước UI đầu tiên.
- Modify `app/src/main/java/com/example/fooddelivery/ui/profile/ProfileFragment.java`: mở dialog chọn ngôn ngữ.
- Modify `app/src/main/res/layout/profile_fragment.xml`: thêm hàng Ngôn ngữ/Language.
- Modify `app/src/main/AndroidManifest.xml`: launcher Splash, portrait và AppCompat locale persistence.
- Modify `app/build.gradle.kts`: thêm SplashScreen dependency.
- Modify `app/src/main/res/values/strings.xml` và `values-en/strings.xml`: copy locale/splash/profile.

**UI/UX — thành viên 2**

- Modify `app/src/main/res/values/colors.xml`, `themes.xml`, `dimens.xml`: semantic FoodGo tokens.
- Create `app/src/main/res/values-w600dp/dimens.xml`: tablet max width và gutters.
- Modify `app/src/main/res/layout/auth_fragment_login.xml`, `home_fragment.xml`, `profile_fragment.xml`: token hóa và container căn giữa.
- Modify `app/src/main/res/layout/auth_activity.xml`, `main_activity.xml`: background/insets thống nhất.
- Create `app/src/androidTest/java/com/example/fooddelivery/FoodGoLayoutInstrumentedTest.java`.

**Branding & QA — thành viên 3**

- Modify `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml`.
- Create/modify adaptive launcher files dưới `app/src/main/res/mipmap-anydpi-v26/` và fallback mipmap hiện có.
- Modify `app/src/main/res/values/strings.xml`, `values-en/strings.xml`: `app_name=FoodGo`.
- Create `app/src/androidTest/java/com/example/fooddelivery/FoodGoLaunchInstrumentedTest.java`.
- Review toàn bộ file trên sau khi Foundation và UI đã tích hợp.

---

### Task 1: Tạo nhánh tích hợp và ba nhánh nhiệm vụ

**Owner:** Trưởng nhóm

**Files:** Không đổi source.

**Interfaces:**
- Consumes: commit mới nhất đã duyệt trên `dev`.
- Produces: cùng một base SHA cho Foundation và UI; Branding tạo sau hai merge đầu.

- [ ] **Step 1: Xác nhận trạng thái và base**

Run:

```powershell
git status --short
git branch --show-current
git log -1 --oneline dev
```

Expected: chỉ có thay đổi đã biết; `dev` tồn tại và SHA được ghi vào biên bản chia việc.

- [ ] **Step 2: Tạo nhánh tích hợp từ dev**

```powershell
git switch dev
git switch -c feature/foodgo-frontend
git rev-parse HEAD
```

Expected: đang ở `feature/foodgo-frontend`; SHA trùng SHA của `dev` ở Step 1.

- [ ] **Step 3: Tạo nhánh Foundation và UI từ cùng base**

Mỗi thành viên tạo nhánh trong worktree/máy riêng:

```powershell
git switch feature/foodgo-frontend
git switch -c feature/foodgo-foundation
```

Thành viên 2 thay tên nhánh bằng `feature/foodgo-ui`. Không tạo Branding cho đến Task 8.

---

### Task 2: Quyết định session destination bằng Java thuần

**Owner:** Thành viên 1 — Foundation

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/ui/splash/LaunchDestination.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/splash/SessionDestinationResolver.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/splash/SessionDestinationResolverTest.java`

**Interfaces:**
- Consumes: JWT string từ `SessionManager.getToken()` và epoch seconds hiện tại.
- Produces: `LaunchDestination resolve(String token, long nowEpochSeconds)`.

- [ ] **Step 1: Viết test đỏ cho token thiếu, hợp lệ, hết hạn và malformed**

```java
package com.example.fooddelivery.ui.splash;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SessionDestinationResolverTest {
    private final SessionDestinationResolver resolver = new SessionDestinationResolver();

    private String jwt(long exp) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"exp\":" + exp + "}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    @Test public void missingTokenGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve(null, 1000L));
    }

    @Test public void futureExpiryGoesToMain() {
        assertEquals(LaunchDestination.MAIN, resolver.resolve(jwt(2000L), 1000L));
    }

    @Test public void expiredTokenGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve(jwt(999L), 1000L));
    }

    @Test public void malformedTokenGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve("not-a-jwt", 1000L));
    }
}
```

- [ ] **Step 2: Chạy test và xác nhận đỏ**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.splash.SessionDestinationResolverTest"`

Expected: FAIL vì `LaunchDestination` và `SessionDestinationResolver` chưa tồn tại.

- [ ] **Step 3: Viết implementation tối thiểu**

```java
package com.example.fooddelivery.ui.splash;

public enum LaunchDestination { AUTH, MAIN }
```

```java
package com.example.fooddelivery.ui.splash;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SessionDestinationResolver {
    private static final Pattern EXP = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    public LaunchDestination resolve(String token, long nowEpochSeconds) {
        if (token == null || token.trim().isEmpty()) return LaunchDestination.AUTH;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return LaunchDestination.AUTH;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher matcher = EXP.matcher(payload);
            if (!matcher.find()) return LaunchDestination.AUTH;
            long expiry = Long.parseLong(matcher.group(1));
            return expiry > nowEpochSeconds ? LaunchDestination.MAIN : LaunchDestination.AUTH;
        } catch (RuntimeException ignored) {
            return LaunchDestination.AUTH;
        }
    }
}
```

- [ ] **Step 4: Chạy test xanh**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.splash.SessionDestinationResolverTest"`

Expected: PASS, 4 tests.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/splash app/src/test/java/com/example/fooddelivery/ui/splash
git commit -m "feat: resolve FoodGo launch destination"
```

---

### Task 3: Thêm Splash launcher và khóa portrait

**Owner:** Thành viên 1 — Foundation

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/ui/splash/SplashActivity.java`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/res/values/themes.xml`

**Interfaces:**
- Consumes: `SessionDestinationResolver.resolve(String,long)` và `SessionManager.getToken()`.
- Produces: launcher duy nhất, chuyển sang `MainActivity` hoặc `AuthActivity` rồi gọi `finish()`.

- [ ] **Step 1: Thêm dependency SplashScreen**

Thêm vào `dependencies` của `app/build.gradle.kts`:

```kotlin
implementation("androidx.core:core-splashscreen:1.0.1")
```

- [ ] **Step 2: Tạo SplashActivity không delay giả tạo**

```java
package com.example.fooddelivery.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.ui.auth.AuthActivity;

public final class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        SessionManager sessions = new SessionManager(getApplicationContext());
        LaunchDestination destination = new SessionDestinationResolver().resolve(
                sessions.getToken(), System.currentTimeMillis() / 1000L);
        Class<?> target = destination == LaunchDestination.MAIN
                ? MainActivity.class : AuthActivity.class;
        startActivity(new Intent(this, target));
        finish();
    }
}
```

- [ ] **Step 3: Cấu hình launcher và portrait trong Manifest**

Chuyển intent-filter `MAIN`/`LAUNCHER` từ `AuthActivity` sang `SplashActivity`. Thêm `android:screenOrientation="portrait"` cho `SplashActivity`, `AuthActivity`, `MainActivity`, `MenuActivity` và `Checkout`. `SplashActivity` dùng `@style/Theme.FoodGo.Starting` và `android:exported="true"`; các Activity không-launcher giữ exported hiện có.

- [ ] **Step 4: Thêm starting theme**

Thêm vào `values/themes.xml`:

```xml
<style name="Theme.FoodGo.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/foodgo_coral</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.FoodDelivery</item>
</style>
```

Nếu `foodgo_coral` chưa có vì UI branch chưa merge, Foundation tạo đúng một resource tạm `#F2644B`; UI branch sẽ sở hữu token cuối.

- [ ] **Step 5: Build và commit**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`; manifest merge không có hai launcher Activity.

```powershell
git add app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/example/fooddelivery/ui/splash app/src/main/res/values/themes.xml app/src/main/res/values/colors.xml
git commit -m "feat: add FoodGo splash routing"
```

---

### Task 4: Thêm locale store và lựa chọn ngôn ngữ trong Profile

**Owner:** Thành viên 1 — Foundation

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/data/local/prefs/LocaleStore.java`
- Create: `app/src/test/java/com/example/fooddelivery/data/local/prefs/LocaleStoreTest.java`
- Modify: `app/src/main/java/com/example/fooddelivery/App.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/profile/ProfileFragment.java`
- Modify: `app/src/main/res/layout/profile_fragment.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: language tag do Profile chọn.
- Produces: `normalize(String)` chỉ trả `vi` hoặc `en`; `apply(String)` gọi AppCompat locale API.

- [ ] **Step 1: Viết test đỏ cho normalization**

```java
package com.example.fooddelivery.data.local.prefs;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LocaleStoreTest {
    @Test public void defaultsToVietnamese() {
        assertEquals("vi", LocaleStore.normalize(null));
        assertEquals("vi", LocaleStore.normalize("fr"));
    }

    @Test public void acceptsEnglish() {
        assertEquals("en", LocaleStore.normalize("en"));
    }
}
```

- [ ] **Step 2: Chạy đỏ rồi thêm LocaleStore**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.data.local.prefs.LocaleStoreTest"`

Expected: FAIL vì `LocaleStore` chưa tồn tại.

```java
package com.example.fooddelivery.data.local.prefs;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LocaleStore {
    private LocaleStore() {}

    public static String normalize(String tag) {
        return "en".equals(tag) ? "en" : "vi";
    }

    public static void apply(String tag) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(normalize(tag)));
    }
}
```

Run lại cùng lệnh. Expected: PASS, 2 tests.

- [ ] **Step 3: Áp dụng Tiếng Việt mặc định trong App**

Trong `App.onCreate()`, trước khi khởi tạo SDK khác:

```java
if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
    LocaleStore.apply("vi");
}
```

Import `AppCompatDelegate` và `LocaleStore`. AppCompat tự persist locale; không tạo SharedPreferences thứ hai.

Trong `<application>` của Manifest, bật auto-storage cho Android 12 trở xuống:

```xml
<service
    android:name="androidx.appcompat.app.AppLocalesMetadataHolderService"
    android:enabled="false"
    android:exported="false">
    <meta-data
        android:name="autoStoreLocales"
        android:value="true" />
</service>
```

- [ ] **Step 4: Thêm row ngôn ngữ và dialog đơn chọn**

Thêm `LinearLayout` id `btnLanguageItem`, chiều cao tối thiểu `56dp`, label `@string/profile_language`, value id `tvLanguageValue` vào nhóm cài đặt của `profile_fragment.xml`. Trong `ProfileFragment.onViewCreated`:

```java
TextView languageValue = view.findViewById(R.id.tvLanguageValue);
String current = AppCompatDelegate.getApplicationLocales().toLanguageTags();
boolean english = current.startsWith("en");
languageValue.setText(english ? R.string.language_english : R.string.language_vietnamese);
view.findViewById(R.id.btnLanguageItem).setOnClickListener(v -> {
    String[] tags = {"vi", "en"};
    int selected = AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith("en") ? 1 : 0;
    new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_language)
            .setSingleChoiceItems(R.array.supported_languages, selected, (dialog, which) -> {
                LocaleStore.apply(tags[which]);
                dialog.dismiss();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
});
```

Thêm imports `AppCompatDelegate`, `MaterialAlertDialogBuilder`, `LocaleStore`.

- [ ] **Step 5: Thêm resources Việt/Anh**

Trong `values/strings.xml`:

```xml
<string name="app_name">FoodGo</string>
<string name="profile_language">Ngôn ngữ</string>
<string name="language_vietnamese">Tiếng Việt</string>
<string name="language_english">English</string>
<string-array name="supported_languages"><item>Tiếng Việt</item><item>English</item></string-array>
```

Trong `values-en/strings.xml`:

```xml
<string name="app_name">FoodGo</string>
<string name="profile_language">Language</string>
<string name="language_vietnamese">Vietnamese</string>
<string name="language_english">English</string>
<string-array name="supported_languages"><item>Vietnamese</item><item>English</item></string-array>
```

- [ ] **Step 6: Chạy tests/build và commit**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: cả hai `BUILD SUCCESSFUL`.

```powershell
git add app/src/main/AndroidManifest.xml app/src/main/java/com/example/fooddelivery/App.java app/src/main/java/com/example/fooddelivery/data/local/prefs/LocaleStore.java app/src/main/java/com/example/fooddelivery/ui/profile/ProfileFragment.java app/src/main/res/layout/profile_fragment.xml app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml app/src/test/java/com/example/fooddelivery/data/local/prefs/LocaleStoreTest.java
git commit -m "feat: add profile language selection"
```

---

### Task 5: Tạo FoodGo design tokens và tablet width contract

**Owner:** Thành viên 2 — UI/UX

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/dimens.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/values-w600dp/dimens.xml`

**Interfaces:**
- Consumes: resource names `foodgo_coral` và `Theme.FoodGo.Starting` từ Foundation.
- Produces: semantic tokens dùng chung cho Splash/Login/Home/Profile.

- [ ] **Step 1: Thêm color tokens**

```xml
<color name="foodgo_coral">#F2644B</color>
<color name="foodgo_brick">#B9362B</color>
<color name="foodgo_surface">#FFF8F5</color>
<color name="foodgo_surface_container">#FFFFFF</color>
<color name="foodgo_text_primary">#241815</color>
<color name="foodgo_text_secondary">#6F5A54</color>
<color name="foodgo_outline">#DCC7C0</color>
<color name="foodgo_error">#B3261E</color>
```

- [ ] **Step 2: Thêm phone/tablet dimensions**

Trong `values/dimens.xml`:

```xml
<dimen name="foodgo_content_max_width">600dp</dimen>
<dimen name="foodgo_screen_gutter">16dp</dimen>
<dimen name="foodgo_touch_target">48dp</dimen>
<dimen name="foodgo_card_radius">18dp</dimen>
<dimen name="foodgo_space_s">8dp</dimen>
<dimen name="foodgo_space_m">16dp</dimen>
<dimen name="foodgo_space_l">24dp</dimen>
```

Trong `values-w600dp/dimens.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="foodgo_content_max_width">640dp</dimen>
    <dimen name="foodgo_screen_gutter">32dp</dimen>
</resources>
```

- [ ] **Step 3: Map theme attributes**

Trong `Theme.FoodDelivery`, map `colorPrimary`, `colorPrimaryVariant`, `colorSecondary`, `android:colorAccent`, `android:windowBackground` lần lượt sang `foodgo_coral`, `foodgo_brick`, `foodgo_coral`, `foodgo_coral`, `foodgo_surface`. Không đổi parent theme trong task này.

- [ ] **Step 4: Build resource merge và commit**

Run: `.\gradlew.bat :app:processDebugResources`

Expected: `BUILD SUCCESSFUL`, không duplicate resource.

```powershell
git add app/src/main/res/values app/src/main/res/values-w600dp
git commit -m "style: add FoodGo design tokens"
```

---

### Task 6: Áp dụng UI thống nhất cho Login, Home và Profile

**Owner:** Thành viên 2 — UI/UX

**Files:**
- Modify: `app/src/main/res/layout/auth_fragment_login.xml`
- Modify: `app/src/main/res/layout/home_fragment.xml`
- Modify: `app/src/main/res/layout/profile_fragment.xml`
- Modify: `app/src/main/res/layout/auth_activity.xml`
- Modify: `app/src/main/res/layout/main_activity.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: semantic tokens Task 5 và `btnLanguageItem` contract Task 4.
- Produces: root content id `foodgoContent` trên ba màn hình, có max width và không phá các view id mà Java đang dùng.

- [ ] **Step 1: Bảo toàn contract view id**

Trước khi sửa, chạy:

```powershell
rg -n "R\.id\." app/src/main/java/com/example/fooddelivery/ui/auth/LoginFragment.java app/src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java app/src/main/java/com/example/fooddelivery/ui/profile/ProfileFragment.java
```

Ghi danh sách id. Không xóa/đổi tên bất kỳ id nào trong danh sách.

- [ ] **Step 2: Thêm container căn giữa**

Với mỗi màn hình, root scroll/content giữ `match_parent`; bọc cột nội dung bằng container có:

```xml
android:id="@+id/foodgoContent"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:layout_gravity="top|center_horizontal"
android:layout_marginHorizontal="@dimen/foodgo_screen_gutter"
android:maxWidth="@dimen/foodgo_content_max_width"
```

Nếu parent không tôn trọng `maxWidth`, dùng `androidx.constraintlayout.widget.ConstraintLayout` và `app:layout_constraintWidth_max="@dimen/foodgo_content_max_width"` với start/end constraint vào parent.

- [ ] **Step 3: Thay literal màu/khoảng cách trong phạm vi**

Thay màu nền/text/primary action bằng `foodgo_surface`, `foodgo_surface_container`, `foodgo_text_primary`, `foodgo_text_secondary`, `foodgo_coral`, `foodgo_brick`, `foodgo_outline`. Các hàng bấm có `minHeight="@dimen/foodgo_touch_target"`; icon-only controls có `contentDescription` từ string resources.

- [ ] **Step 4: Resource hóa chuỗi hiển thị bị chạm**

Mọi `android:text`, `android:hint`, `android:contentDescription` literal trong năm layout trên chuyển sang `@string/...`, có bản Việt trong `values` và bản Anh trong `values-en`. Không dịch tên Restaurant/Món lấy từ server.

- [ ] **Step 5: Kiểm tra lint resources và build**

Run:

```powershell
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

Expected: không có lỗi XML/resource; build thành công. Warning cũ ngoài file chạm được ghi riêng, không che giấu.

- [ ] **Step 6: Commit**

```powershell
git add app/src/main/res/layout app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "style: unify FoodGo account surfaces"
```

---

### Task 7: Tích hợp Foundation và UI

**Owner:** Trưởng nhóm

**Files:** Các file dùng chung trong Manifest, Profile và values resources.

**Interfaces:**
- Consumes: hai nhánh đã build xanh.
- Produces: `feature/foodgo-frontend` chứa routing, locale và UI contract đồng nhất.

- [ ] **Step 1: Review và merge Foundation**

```powershell
git switch feature/foodgo-frontend
git merge --no-ff feature/foodgo-foundation
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: merge thành công và hai lệnh build xanh.

- [ ] **Step 2: Review và merge UI**

```powershell
git merge --no-ff feature/foodgo-ui
```

Khi conflict, giữ logic/id `btnLanguageItem` từ Foundation và semantic tokens/layout styling từ UI. Không chọn nguyên một phía cho `profile_fragment.xml`, `colors.xml`, `strings.xml` hoặc `themes.xml`.

- [ ] **Step 3: Xác minh sau merge**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

Expected: tất cả thành công; `git diff --check` không báo whitespace error.

---

### Task 8: Tạo adaptive icon FG và app label FoodGo

**Owner:** Thành viên 3 — Branding & QA

**Files:**
- Modify: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Modify: `app/src/main/res/drawable/ic_launcher_background.xml`
- Create/Modify: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create/Modify: `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: `foodgo_coral`, `foodgo_brick`; launcher routing đã tích hợp.
- Produces: adaptive icon resources và `@string/app_name`.

- [ ] **Step 1: Tạo nhánh từ integration mới nhất**

```powershell
git switch feature/foodgo-frontend
git switch -c feature/foodgo-branding-qa
```

- [ ] **Step 2: Tạo vector FG trong safe zone**

Giữ viewport `108x108`; monogram nằm trong vùng trung tâm 66x66 để không bị mask cắt. `ic_launcher_background.xml` dùng `foodgo_brick`; foreground dùng shape `FG` màu trắng hoặc kem với path vector gốc do thành viên tạo, không dùng font runtime và không sao chép logo bên thứ ba.

- [ ] **Step 3: Cấu hình adaptive icon**

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

Dùng cùng nội dung cho `ic_launcher.xml` và `ic_launcher_round.xml`; launcher mask quyết định hình tròn/vuông.

- [ ] **Step 4: App label qua resource**

Manifest dùng `android:label="@string/app_name"`; cả `values` và `values-en` đặt `app_name` là `FoodGo`.

- [ ] **Step 5: Build và commit branding**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`, không missing mipmap/drawable.

```powershell
git add app/src/main/AndroidManifest.xml app/src/main/res/drawable app/src/main/res/mipmap-anydpi-v26 app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: add FoodGo FG branding"
```

---

### Task 9: Instrumented regression tests và audit cuối

**Owner:** Thành viên 3 — Branding & QA

**Files:**
- Create: `app/src/androidTest/java/com/example/fooddelivery/FoodGoLaunchInstrumentedTest.java`
- Create: `app/src/androidTest/java/com/example/fooddelivery/FoodGoLayoutInstrumentedTest.java`
- Modify: chỉ các file phạm vi đã duyệt khi test phát hiện lỗi.

**Interfaces:**
- Consumes: build tích hợp sau Task 8.
- Produces: bằng chứng routing, locale, portrait và accessibility cơ bản.

- [ ] **Step 1: Viết test launcher/portrait**

```java
package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.fooddelivery.ui.splash.SplashActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FoodGoLaunchInstrumentedTest {
    @Test public void splashIsPortrait() throws PackageManager.NameNotFoundException {
        android.content.Context context = ApplicationProvider.getApplicationContext();
        ActivityInfo info = context.getPackageManager().getActivityInfo(
                new ComponentName(context, SplashActivity.class), 0);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, info.screenOrientation);
    }
}
```

- [ ] **Step 2: Viết test touch target của language row**

```java
package com.example.fooddelivery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.fooddelivery.ui.auth.AuthActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FoodGoLayoutInstrumentedTest {
    @Rule public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(AuthActivity.class);

    @Test public void loginRootIsDisplayed() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }
}
```

- [ ] **Step 3: Chạy unit/build/instrumented tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Expected: unit/build xanh; instrumented tests xanh khi emulator/device khả dụng. Nếu không có device, báo chính xác `No connected devices` và không tuyên bố instrumented tests pass.

- [ ] **Step 4: Audit thủ công theo ma trận**

Kiểm tra phone nhỏ, phone chuẩn, tablet 7–10 inch: Việt mặc định; đổi English; kill/relaunch; đổi lại Việt; session hợp lệ/hết hạn/không có; landscape bị chặn; text scale 1.3x; icon mask tròn/vuông. Ghi PASS/FAIL cho từng ô và file issue cho FAIL chưa sửa.

- [ ] **Step 5: Commit QA**

```powershell
git add app/src/androidTest
git commit -m "test: cover FoodGo launch and layouts"
```

---

### Task 10: Tích hợp Branding/QA và chuẩn bị merge về dev

**Owner:** Trưởng nhóm

**Files:** Không thêm phạm vi mới.

**Interfaces:**
- Consumes: `feature/foodgo-branding-qa` đã bàn giao.
- Produces: nhánh `feature/foodgo-frontend` sẵn sàng review/PR.

- [ ] **Step 1: Merge nhánh cuối**

```powershell
git switch feature/foodgo-frontend
git merge --no-ff feature/foodgo-branding-qa
```

Expected: Manifest giữ launcher Splash + portrait + label/icon FoodGo.

- [ ] **Step 2: Chạy verification mới nhất**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
git diff --check dev...HEAD
git status --short
```

Expected: ba Gradle task thành công; không whitespace error; worktree sạch ngoài thay đổi đã biết.

- [ ] **Step 3: Review phạm vi**

```powershell
git diff --stat dev...HEAD
git diff --name-only dev...HEAD
```

Xác nhận không có migration/schema/API/order-domain file. Tìm hard-coded strings trong các layout đã chạm và xác nhận các ngoại lệ chỉ là dữ liệu preview `tools:text`.

- [ ] **Step 4: Bàn giao để người dùng quyết định merge/PR**

Báo cáo: commit theo thứ tự, conflict và cách giải quyết, test commands/output, ma trận device/locale/session, rủi ro còn lại. Không tự push hoặc merge `feature/foodgo-frontend` vào `dev`.
