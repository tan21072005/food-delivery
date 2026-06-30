# AI Chatbot Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the approved orange NGBT chatbot as the third/center Android bottom-navigation tab, with deterministic state, message/history UI, feedback, retry, and authenticated Supabase integration.

**Architecture:** `ChatFragment` renders one immutable `ChatUiState` owned by `ChatViewModel`. The ViewModel calls the existing `ChatRepositoryContract`, rejects concurrent sends, and uses an operation generation to ignore stale callbacks. `ListAdapter` implementations render typed rows; history and conversation actions live in focused bottom sheets.

**Tech Stack:** Java 11, Android Views/View Binding, Navigation Component, LiveData/ViewModel, RecyclerView/ListAdapter/DiffUtil, Material Components, Retrofit/Gson, JUnit 4.

## Global Constraints

- Follow `docs/superpowers/specs/2026-06-29-ai-chatbot-frontend-design.md`.
- Visible title is exactly `Chat bot trợ lý NGBT`.
- Bottom-navigation order is Home, Orders, AI Assistant, Favorites, Profile.
- AI Assistant is a normal third `BottomNavigationView` destination, not a floating button.
- Use `#F5A623`, `#FFD580`, `#FFF8E1`, `#FFFFFF`, `#1A1A1A`, and `#888888`; do not introduce pink or purple into chatbot resources.
- The composer placeholder is exactly `Hỏi về Món hoặc Order...`.
- Chat requires a valid saved JWT; anonymous chat is not allowed.
- Text only, maximum 1,000 characters, one send request at a time.
- Never add an OpenAI key or Supabase service-role key to Android.
- Preserve unrelated password-recovery changes in the working tree.

---

## File Map

### State

- Create `app/src/main/java/com/example/fooddelivery/ui/chat/ChatUiState.java` — immutable screen state.
- Create `app/src/main/java/com/example/fooddelivery/ui/chat/ChatEvent.java` — one-shot session/navigation events.
- Create `app/src/main/java/com/example/fooddelivery/ui/chat/ChatViewModel.java` — state transitions and stale-callback protection.
- Modify `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatMessage.java` — add a constructor for optimistic/test messages.
- Modify `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatConversation.java` — add a constructor for tests.
- Test `app/src/test/java/com/example/fooddelivery/ui/chat/ChatViewModelTest.java`.

### Navigation and resources

- Create `app/src/main/res/navigation/nav_chat.xml`.
- Modify `app/src/main/res/navigation/nav_main.xml`.
- Modify `app/src/main/res/menu/bottom_nav_menu.xml`.
- Modify `app/src/main/res/values/strings.xml`.
- Modify `app/src/main/res/values-en/strings.xml`.
- Modify `app/src/main/res/values/colors.xml`.
- Create vectors `ic_chat_assistant.xml`, `ic_chat_history.xml`, `ic_chat_new.xml`, `ic_chat_send.xml`, `ic_feedback_up.xml`, `ic_feedback_down.xml`, `ic_more.xml`.
- Create backgrounds `bg_chat_user.xml`, `bg_chat_assistant.xml`, `bg_chat_composer.xml`, `bg_chat_suggestion.xml`.
- Create layouts `fragment_chat.xml`, `item_chat_day.xml`, `item_chat_user.xml`, `item_chat_assistant.xml`, `item_chat_typing.xml`.
- Test `app/src/test/java/com/example/fooddelivery/ui/chat/ChatResourceContractTest.java`.

### Rows and sheets

- Create `ui/chat/adapters/ChatRow.java`, `ChatRowFactory.java`, `ChatMessageAdapter.java`, `ChatHistoryAdapter.java`.
- Create `ui/chat/ChatHistoryBottomSheet.java`, `ChatOptionsBottomSheet.java`.
- Create layouts `bottom_sheet_chat_history.xml`, `item_chat_history.xml`, `bottom_sheet_chat_options.xml`.
- Test `app/src/test/java/com/example/fooddelivery/ui/chat/ChatRowFactoryTest.java`.

### Screen binding

- Create `app/src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java`.
- Test `app/src/test/java/com/example/fooddelivery/ui/chat/ChatFragmentContractTest.java`.

---

### Task 1: Implement deterministic chat state

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatUiState.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatEvent.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatViewModel.java`
- Modify: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatMessage.java`
- Modify: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatConversation.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatViewModelTest.java`

**Interfaces:**

- Consumes: `ChatRepositoryContract`, `ChatFailure`, `ChatSendResponse`.
- Produces:

```java
public final class ChatUiState {
    public ChatConversation conversation();
    public List<ChatConversation> conversations();
    public List<ChatMessage> messages();
    public String draft();
    public boolean loadingHistory();
    public boolean sending();
    public String pendingText();
    public String failedText();
    public String failedRequestId();
    public String errorMessage();
    public Map<Long, Integer> feedback();
}

public enum ChatEvent { SESSION_EXPIRED }

public interface RequestIdFactory {
    String create();
}

public final class ChatViewModel extends ViewModel {
    public ChatViewModel(ChatRepositoryContract repository, long userId,
                         RequestIdFactory requestIds);
    public LiveData<ChatUiState> state();
    public LiveData<ChatEvent> events();
    public void loadConversations();
    public void selectConversation(ChatConversation conversation);
    public void newConversation();
    public void updateDraft(String value);
    public void send();
    public void retry();
    public void setFeedback(long messageId, int value);
    public void renameConversation(String title);
    public void deleteConversation();
    public void consumeEvent();
}
```

- [ ] **Step 1: Add model constructors**

Add to `ChatMessage`:

```java
public ChatMessage(long id, String conversationId, String role, String content,
                   String status, String createdAt) {
    this.id = id;
    this.conversationId = conversationId;
    this.role = role;
    this.content = content;
    this.status = status;
    this.createdAt = createdAt;
}
```

Add the equivalent four-field constructor to `ChatConversation`.

- [ ] **Step 2: Write the first failing ViewModel test**

Use a fake `ChatRepositoryContract` that captures callbacks. Verify:

```java
viewModel.updateDraft("  Gợi ý Món trưa  ");
viewModel.send();

assertEquals("Gợi ý Món trưa", repository.sentText);
assertTrue(viewModel.state().getValue().sending());
assertEquals("", viewModel.state().getValue().draft());

repository.sendCallback.onSuccess(successResponse());
assertFalse(viewModel.state().getValue().sending());
assertEquals(2, viewModel.state().getValue().messages().size());
```

- [ ] **Step 3: Run the focused test and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatViewModelTest"
```

Expected: compile failure because the chat state classes do not exist.

- [ ] **Step 4: Implement immutable state and send**

Use copy-style methods returning a new `ChatUiState`. In `send()`:

```java
String text = state.getValue().draft().trim();
if (text.isEmpty() || text.length() > 1000 || state.getValue().sending()) return;
String requestId = requestIds.create();
long generation = ++operationGeneration;
String conversationId = selected == null ? null : selected.getId();
state.setValue(current.withSending(text, requestId));
repository.sendMessage(conversationId, text, requestId,
        new ResultCallback<ChatSendResponse>() {
            @Override public void onSuccess(ChatSendResponse value) {
                if (generation != operationGeneration) return;
                state.setValue(state.getValue().withSendSuccess(value));
            }
            @Override public void onError(ChatFailure failure) {
                if (generation != operationGeneration) return;
                if (failure.statusCode() == 401) {
                    events.setValue(ChatEvent.SESSION_EXPIRED);
                }
                state.setValue(state.getValue().withSendFailure(
                        text, requestId, failure.userMessage()));
            }
        });
```

`withSending` stores `pendingText=text`; `ChatRowFactory` renders that temporary
Customer row immediately followed by `Typing`. `withSendSuccess` clears the
temporary row and appends the persisted user/assistant messages returned by
the backend.

- [ ] **Step 5: Add retry and stale-callback tests**

Verify retry reuses `failedRequestId`, concurrent `send()` is ignored, and:

```java
viewModel.send();
ChatRepositoryContract.ResultCallback<ChatSendResponse> old = repository.sendCallback;
viewModel.newConversation();
old.onSuccess(successResponse());
assertNull(viewModel.state().getValue().conversation());
assertTrue(viewModel.state().getValue().messages().isEmpty());
```

- [ ] **Step 6: Implement history, conversation actions, feedback, retry, and events**

Increment `operationGeneration` in `newConversation()` and
`selectConversation()`. `setFeedback()` passes the injected numeric Customer
ID to `repository.setFeedback()` and updates `feedback` only after success.
`renameConversation()` replaces the selected/list row returned by the
repository. `deleteConversation()` removes the selected row and then calls
`newConversation()` after repository success.

- [ ] **Step 7: Run state tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatViewModelTest"
```

Expected: all `ChatViewModelTest` tests pass.

- [ ] **Step 8: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/chat/ChatUiState.java app/src/main/java/com/example/fooddelivery/ui/chat/ChatEvent.java app/src/main/java/com/example/fooddelivery/ui/chat/ChatViewModel.java app/src/main/java/com/example/fooddelivery/data/model/chat/ChatMessage.java app/src/main/java/com/example/fooddelivery/data/model/chat/ChatConversation.java app/src/test/java/com/example/fooddelivery/ui/chat/ChatViewModelTest.java
git commit -m "feat: add deterministic chatbot state"
```

---

### Task 2: Add center navigation and orange visual resources

**Files:**

- Create/modify every file listed under “Navigation and resources”.

**Interfaces:**

- Consumes: existing `MainActivity` + `NavigationUI`.
- Produces: destination ID `nav_chat`, generated `FragmentChatBinding`, and all row bindings.

- [ ] **Step 1: Write a failing resource contract**

Read XML text and assert:

```java
assertTrue(menu.indexOf("@id/nav_ordes") < menu.indexOf("@id/nav_chat"));
assertTrue(menu.indexOf("@id/nav_chat") < menu.indexOf("@id/nav_favorites"));
assertTrue(mainGraph.contains("@navigation/nav_chat"));
assertTrue(strings.contains("<string name=\"chat_title\">Chat bot trợ lý NGBT</string>"));
assertFalse(fragmentLayout.contains("android:text=\"#"));
```

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatResourceContractTest"
```

Expected: fail because `nav_chat.xml` and chatbot resources do not exist.

- [ ] **Step 3: Add navigation**

Add the third menu item:

```xml
<item
    android:id="@id/nav_chat"
    android:icon="@drawable/ic_chat_assistant"
    android:title="@string/nav_chat" />
```

Create `nav_chat.xml` with start destination `chatFragment` whose ID is
`nav_chat`, then include it in `nav_main.xml` between orders and favorites.
Use graph ID `nav_chat` and start destination ID `chatFragment`, matching the
existing `nav_home`/`homeFragment` nesting pattern.

- [ ] **Step 4: Add exact strings and colors**

Add Vietnamese copy from the design spec and English equivalents. Add:

```xml
<color name="chat_orange">#F5A623</color>
<color name="chat_orange_warm">#FFD580</color>
<color name="chat_assistant_wash">#FFF8E1</color>
<color name="chat_paper">#FFFFFF</color>
<color name="chat_ink">#1A1A1A</color>
<color name="chat_muted">#888888</color>
```

- [ ] **Step 5: Create screen and row layouts**

`fragment_chat.xml` must use this hierarchy and IDs:

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/chatToolbar" />
    <LinearLayout android:id="@+id/emptyState">
        <TextView android:id="@+id/welcomeText" />
        <com.google.android.material.chip.ChipGroup
            android:id="@+id/suggestionGroup" />
    </LinearLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/messageList" />
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/composerCard">
        <EditText android:id="@+id/messageInput" />
        <com.google.android.material.button.MaterialButton
            android:id="@+id/sendButton" />
    </com.google.android.material.card.MaterialCardView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

Constrain `composerCard` to the screen bottom; `main_activity.xml` already pads
the NavHost above bottom navigation. Use `adjustResize` behavior so the keyboard
moves the composer.

- [ ] **Step 6: Create vectors and shape drawables**

Use 24dp vectors with `chat_ink` or `chat_orange`; use 20dp user-bubble corners,
16dp assistant corners, and a 28dp white composer with a subtle 1dp
`#EEEEEE` stroke.

- [ ] **Step 7: Verify resources and assemble**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatResourceContractTest"
.\gradlew.bat :app:assembleDebug
```

Expected: contract passes and debug APK assembles.

- [ ] **Step 8: Commit**

Stage only the navigation/resource/layout files from this task and commit:

```powershell
git commit -m "feat: add chatbot center navigation and layout"
```

---

### Task 3: Transform messages into typed rows

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatRow.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatRowFactory.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatMessageAdapter.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatHistoryAdapter.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatRowFactoryTest.java`

**Interfaces:**

```java
public abstract class ChatRow {
    public abstract long stableId();
    public static final class Day extends ChatRow { public String label(); }
    public static final class Customer extends ChatRow { public ChatMessage message(); }
    public static final class Assistant extends ChatRow {
        public ChatMessage message();
        public Integer feedback();
    }
    public static final class Typing extends ChatRow {}
    public static final class Failed extends ChatRow {
        public String content();
        public String message();
    }
}

public final class ChatRowFactory {
    public List<ChatRow> create(ChatUiState state, ZoneId zoneId);
}
```

- [ ] **Step 1: Write the failing row transformation test**

Given messages on two local dates plus `sending=true` and `pendingText`, expect:

```text
Day, Customer, Assistant, Day, Customer, Typing
```

Also verify only `Assistant` carries feedback and failed state produces one
`Failed` row with retry content.

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatRowFactoryTest"
```

- [ ] **Step 3: Implement row types and factory**

Parse ISO timestamps with `Instant.parse`, convert through the injected
`ZoneId`, emit one `Day` before the first message of each date, then append
`Typing` or `Failed`. Stable IDs:

```java
day.toEpochDay() * 10;
message.getId() * 10 + 1; // Customer
message.getId() * 10 + 2; // Assistant
Long.MAX_VALUE - 1;       // Typing
Long.MAX_VALUE - 2;       // Failed
```

- [ ] **Step 4: Implement adapters**

`ChatMessageAdapter` extends `ListAdapter<ChatRow, RecyclerView.ViewHolder>`,
enables stable IDs, and exposes listeners:

```java
interface Listener {
    void onFeedback(long messageId, int value);
    void onRetry();
}
```

`ChatHistoryAdapter` exposes `onSelect(ChatConversation)` and
`onOptions(ChatConversation)`.

- [ ] **Step 5: Run tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatRowFactoryTest"
```

Expected: all row factory tests pass.

- [ ] **Step 6: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/chat/adapters app/src/test/java/com/example/fooddelivery/ui/chat/ChatRowFactoryTest.java
git commit -m "feat: render typed chatbot rows"
```

---

### Task 4: Bind the screen and conversation sheets

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatOptionsBottomSheet.java`
- Create: `app/src/main/res/layout/bottom_sheet_chat_history.xml`
- Create: `app/src/main/res/layout/item_chat_history.xml`
- Create: `app/src/main/res/layout/bottom_sheet_chat_options.xml`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatFragmentContractTest.java`

**Interfaces:**

- Consumes: `ChatViewModel`, generated bindings, row/history adapters.
- Produces: interactive screen matching the approved flow.

- [ ] **Step 1: Write a failing Fragment contract test**

Source-contract assertions:

```java
assertTrue(source.contains("binding = FragmentChatBinding.inflate"));
assertTrue(source.contains("binding = null"));
assertTrue(source.contains("viewModel.updateDraft"));
assertTrue(source.contains("viewModel.send()"));
assertTrue(source.contains("ChatHistoryBottomSheet"));
assertTrue(source.contains("ChatEvent.SESSION_EXPIRED"));
```

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatFragmentContractTest"
```

- [ ] **Step 3: Implement binding lifecycle and authentication**

In `onViewCreated`, create `SessionManager`. If `!session.isLoggedIn()`,
navigate to `R.id.loginFragment` and return. Otherwise create the ViewModel
with a factory injecting `new ChatRepository(requireContext())`,
`session.getUserId()`, and `() -> UUID.randomUUID().toString()`.

Set toolbar menu actions:

```java
R.id.action_chat_history -> show history sheet
R.id.action_chat_new -> viewModel.newConversation()
```

Clear `_binding` in `onDestroyView`.

- [ ] **Step 4: Bind composer, suggestions, rows, and events**

- Text watcher calls `viewModel.updateDraft`.
- Send IME action and button call `viewModel.send()`.
- Suggestion chips set the draft but do not auto-send.
- Adapter feedback/retry callbacks call ViewModel methods.
- State observer toggles empty/list visibility, submits rows, disables sending,
  renders errors with `Snackbar`, and scrolls only when a row was appended.
- `SESSION_EXPIRED` navigates to login and calls `consumeEvent()`.

- [ ] **Step 5: Implement history and options sheets**

`ChatHistoryBottomSheet` uses `BottomSheetDialogFragment`, limits expanded
height to roughly 70%, lists newest first, and has `Trò chuyện mới`.
`ChatOptionsBottomSheet` exposes rename and delete. Delete opens a
`MaterialAlertDialog` confirmation before calling the ViewModel.

- [ ] **Step 6: Run focused tests and build**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*Chat*Test"
.\gradlew.bat :app:assembleDebug
```

Expected: chatbot tests pass and APK assembles.

- [ ] **Step 7: Commit**

```powershell
git add app/src/main/java/com/example/fooddelivery/ui/chat app/src/main/res/layout/bottom_sheet_chat_history.xml app/src/main/res/layout/item_chat_history.xml app/src/main/res/layout/bottom_sheet_chat_options.xml app/src/test/java/com/example/fooddelivery/ui/chat/ChatFragmentContractTest.java
git commit -m "feat: complete chatbot interactions"
```

---

### Task 5: Full verification

**Files:**

- Modify only if verification reveals a chatbot defect.

**Interfaces:**

- Produces fresh evidence that frontend, existing app behavior, and secret
  boundaries remain valid.

- [ ] **Step 1: Run all Android unit tests**

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Build the debug APK**

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL` and
`app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 3: Scan Android for server secrets**

```powershell
rg -n "OPENAI_API_KEY|SUPABASE_SERVICE_ROLE|sk-[A-Za-z0-9_-]{16,}" app/src/main
```

Expected: no matches.

- [ ] **Step 4: Manual emulator/device flow**

Verify:

1. Bottom navigation has five items and AI is third.
2. Header has title, history, and new-chat actions; no back/home action.
3. Composer remains visible above bottom nav and keyboard.
4. Send success renders Customer + assistant rows and feedback.
5. History select/new/rename/delete work.
6. Network failure leaves a retryable question.
7. Expired JWT navigates to login.
8. Rotation preserves the selected conversation.

- [ ] **Step 5: Record evidence**

Add a short verification section to `docs/chatbot-backend-setup.md` with the
test/build commands, APK path, manual device used, and any live deployment
prerequisite still outstanding.

- [ ] **Step 6: Commit verification-only fixes or docs**

```powershell
git add docs/chatbot-backend-setup.md
git commit -m "docs: record chatbot frontend verification"
```
