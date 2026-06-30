# AI Chatbot Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the real “Chat bot trợ lý NGBT” Android experience as the center bottom-navigation tab, backed by the authenticated Supabase chatbot API.

**Architecture:** A dedicated nested chat navigation graph hosts `ChatFragment`. `ChatViewModel` owns selected-conversation and in-flight operation state; `ChatRepository` owns Retrofit calls; `ListAdapter` implementations render messages and history. Existing `SupabaseClient` supplies the caller JWT and anon key, while the OpenAI key never enters Android.

**Tech Stack:** Java 11, Android SDK 36/minSdk 24, View Binding, Navigation Component, RecyclerView/ListAdapter/DiffUtil, LiveData/ViewModel, Retrofit/Gson/OkHttp, JUnit 4, MockWebServer.

## Global Constraints

- The visible screen title is exactly `Chat bot trợ lý NGBT`.
- Bottom-navigation order is Home, Orders, AI Assistant, Favorites, Profile.
- The AI Assistant is the third/center standard `BottomNavigationView` item.
- Input is plain text, trimmed, and limited to 1,000 characters.
- Only one send operation may run at a time.
- A stale callback must never replace the currently selected conversation.
- The first release does not stream tokens and cannot mutate carts or orders.
- All UI text is Vietnamese with English equivalents in `values-en`.
- Never add an OpenAI key or Supabase service-role key to Android configuration.
- Do not modify unrelated Home, Orders, Favorites, Profile, cart, or auth behavior.
- Do not commit or push unless the user explicitly requests it.

---

## File Map

### Data and API

- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatConversation.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatMessage.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatFeedback.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatSendRequest.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatSendResponse.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/model/chat/ChatApiError.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/remote/apis/ChatApiService.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/repository/ChatFailure.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/repository/ChatRepositoryContract.java`
- Create: `app/src/main/java/com/example/fooddelivery/data/repository/ChatRepository.java`
- Test: `app/src/test/java/com/example/fooddelivery/data/repository/ChatRepositoryTest.java`

### State and UI

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatUiState.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatEvent.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatViewModel.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatOptionsBottomSheet.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatMessageAdapter.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatHistoryAdapter.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatRow.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatRowFactory.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatViewModelTest.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatMessageAdapterContractTest.java`

### Resources and navigation

- Create: `app/src/main/res/navigation/nav_chat.xml`
- Modify: `app/src/main/res/navigation/nav_main.xml`
- Modify: `app/src/main/res/menu/bottom_nav_menu.xml`
- Create: `app/src/main/res/drawable/ic_chat_assistant.xml`
- Create: `app/src/main/res/drawable/ic_chat_history.xml`
- Create: `app/src/main/res/drawable/ic_chat_new.xml`
- Create: `app/src/main/res/drawable/ic_chat_send.xml`
- Create: `app/src/main/res/drawable/ic_feedback_up.xml`
- Create: `app/src/main/res/drawable/ic_feedback_down.xml`
- Create: `app/src/main/res/drawable/bg_chat_user.xml`
- Create: `app/src/main/res/drawable/bg_chat_assistant.xml`
- Create: `app/src/main/res/drawable/bg_chat_composer.xml`
- Create: `app/src/main/res/layout/fragment_chat.xml`
- Create: `app/src/main/res/layout/item_chat_user.xml`
- Create: `app/src/main/res/layout/item_chat_assistant.xml`
- Create: `app/src/main/res/layout/item_chat_day.xml`
- Create: `app/src/main/res/layout/item_chat_typing.xml`
- Create: `app/src/main/res/layout/bottom_sheet_chat_history.xml`
- Create: `app/src/main/res/layout/item_chat_history.xml`
- Create: `app/src/main/res/layout/bottom_sheet_chat_options.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values/colors.xml`

### Task 1: Define the Retrofit contract and repository

**Files:**

- Create all Data and API files listed above.
- Test: `app/src/test/java/com/example/fooddelivery/data/repository/ChatRepositoryTest.java`

**Interfaces:**

- Produces:

```java
public interface ChatRepositoryContract {
    interface ResultCallback<T> {
        void onSuccess(T value);
        void onError(ChatFailure failure);
    }

    void sendMessage(String conversationId, String text, String requestId,
                     ResultCallback<ChatSendResponse> callback);
    void loadConversations(ResultCallback<List<ChatConversation>> callback);
    void loadMessages(String conversationId, ResultCallback<List<ChatMessage>> callback);
    void renameConversation(String conversationId, String title,
                            ResultCallback<ChatConversation> callback);
    void deleteConversation(String conversationId, ResultCallback<Void> callback);
    void setFeedback(long messageId, long userId, int value,
                     ResultCallback<ChatFeedback> callback);
}
```

- Error mapping:

```java
public final class ChatFailure {
    private final int statusCode;
    private final String code;
    private final String userMessage;

    public ChatFailure(int statusCode, String code, String userMessage) {
        this.statusCode = statusCode;
        this.code = code;
        this.userMessage = userMessage;
    }

    public int statusCode() { return statusCode; }
    public String code() { return code; }
    public String userMessage() { return userMessage; }
}
```

- [ ] **Step 1: Write failing repository contract tests**

Use `MockWebServer` and assert:

```java
@Test
public void sendMessage_postsEdgeFunctionContract() throws Exception {
    server.enqueue(json(200, SUCCESS_JSON));
    AtomicReference<ChatSendResponse> result = new AtomicReference<>();

    repository.sendMessage(null, "Gợi ý món trưa", REQUEST_ID,
            callback(result));

    RecordedRequest request = server.takeRequest();
    assertEquals("/functions/v1/chat-assistant", request.getPath());
    assertEquals("POST", request.getMethod());
    assertTrue(request.getBody().readUtf8().contains("\"client_request_id\":\"" + REQUEST_ID + "\""));
    assertNotNull(result.get().getAssistantMessage());
}

@Test
public void loadMessages_filtersConversationAndOrdersAscending() throws Exception {
    server.enqueue(json(200, "[]"));
    repository.loadMessages(CONVERSATION_ID, callback(new AtomicReference<>()));
    String path = server.takeRequest().getPath();
    assertTrue(path.contains("conversation_id=eq." + CONVERSATION_ID));
    assertTrue(path.contains("order=created_at.asc%2Cid.asc"));
}

@Test
public void dailyLimit_mapsToVietnameseMessage() {
    ChatFailure failure = repository.mapError(
            429,
            "{\"error\":{\"code\":\"DAILY_LIMIT_REACHED\",\"message\":\"x\"}}");
    assertEquals("DAILY_LIMIT_REACHED", failure.code());
    assertEquals("Bạn đã dùng hết 15 lượt hỏi hôm nay", failure.userMessage());
}
```

- [ ] **Step 2: Run tests and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatRepositoryTest"
```

Expected: FAIL because chat models/repository do not exist.

- [ ] **Step 3: Implement immutable Gson models**

Use `@SerializedName` for snake_case. Required fields:

```java
public final class ChatConversation {
    private String id;
    private String title;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("updated_at") private String updatedAt;
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}

public final class ChatMessage {
    private long id;
    @SerializedName("conversation_id") private String conversationId;
    private String role;
    private String content;
    private String status;
    @SerializedName("created_at") private String createdAt;
    public long getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
```

`ChatSendRequest` constructor accepts nullable conversation id, message, and
request id. `ChatSendResponse` exposes conversation, user message, assistant
message, and nested usage. `ChatApiError` mirrors
`{"error":{"code":"DAILY_LIMIT_REACHED","message":"Bạn đã dùng hết lượt"}}`.

- [ ] **Step 4: Implement `ChatApiService`**

```java
public interface ChatApiService {
    @POST("functions/v1/chat-assistant")
    Call<ChatSendResponse> sendMessage(@Body ChatSendRequest request);

    @GET("rest/v1/chat_conversations")
    Call<List<ChatConversation>> getConversations(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit);

    @GET("rest/v1/chat_messages")
    Call<List<ChatMessage>> getMessages(
            @Query("conversation_id") String conversationFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/chat_conversations")
    Call<List<ChatConversation>> renameConversation(
            @Query("id") String idFilter,
            @Body Map<String, String> body);

    @DELETE("rest/v1/chat_conversations")
    Call<Void> deleteConversation(@Query("id") String idFilter);

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/chat_feedback?on_conflict=message_id,user_id")
    Call<List<ChatFeedback>> upsertFeedback(@Body ChatFeedback feedback);
}
```

- [ ] **Step 5: Implement repository success/error mapping**

Construct the default repository with:

```java
api = SupabaseClient.getInstance(context).create(ChatApiService.class);
```

Map:

- network failure → `Không thể kết nối. Vui lòng kiểm tra mạng`;
- 401 → `Phiên đăng nhập đã hết hạn`;
- `DAILY_LIMIT_REACHED` → `Bạn đã dùng hết 15 lượt hỏi hôm nay`;
- `UPSTREAM_RATE_LIMIT` → `Trợ lý đang bận. Vui lòng thử lại sau`;
- other 4xx/5xx → `Không thể xử lý yêu cầu. Vui lòng thử lại`.

For list-returning PATCH/POST calls, an empty successful body is an application
error, not success.

- [ ] **Step 6: Run repository tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatRepositoryTest"
```

Expected: all repository tests PASS.

### Task 2: Implement deterministic ViewModel state and stale-callback protection

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatUiState.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatEvent.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatViewModel.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatViewModelTest.java`

**Interfaces:**

- `ChatUiState` exposes immutable lists, selected conversation id, loading-history, sending, and draft-retry text/request id.
- `ChatEvent.Type` is `ERROR`, `SESSION_EXPIRED`, `CONVERSATION_DELETED`, or `SCROLL_TO_BOTTOM`.
- `ChatViewModel` exposes:

```java
LiveData<ChatUiState> getState();
LiveData<ChatEvent> getEvents();
void loadHistory();
void selectConversation(String conversationId);
void startNewConversation();
void send(String rawText);
void retryFailed();
void renameCurrent(String title);
void deleteConversation(String conversationId);
void setFeedback(ChatMessage message, int value);
```

- [ ] **Step 1: Write failing ViewModel tests**

Use fake callbacks that tests complete manually:

```java
@Test
public void send_ignoresSecondSendWhileInFlight() {
    viewModel.send("Một");
    viewModel.send("Hai");
    assertEquals(1, repository.sendCalls.size());
}

@Test
public void staleSendCallback_doesNotReplaceNewConversation() {
    viewModel.send("Câu cũ");
    PendingSend old = repository.sendCalls.get(0);
    viewModel.startNewConversation();
    old.success(responseFor("old-id"));
    assertNull(viewModel.getState().getValue().getSelectedConversationId());
}

@Test
public void retry_reusesClientRequestId() {
    viewModel.send("Gợi ý món");
    PendingSend first = repository.sendCalls.get(0);
    first.failure(new ChatFailure(500, "UPSTREAM_ERROR", "Lỗi"));
    viewModel.retryFailed();
    assertEquals(first.requestId, repository.sendCalls.get(1).requestId);
}
```

Also test 1,001 characters, blank text, history selection, delete-current resets the
screen, 401 emits `SESSION_EXPIRED`, and feedback only accepts assistant messages
with values `-1` or `1`.

- [ ] **Step 2: Run tests and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatViewModelTest"
```

Expected: FAIL because chat state/ViewModel do not exist.

- [ ] **Step 3: Implement state ownership**

Constructors:

```java
public ChatViewModel(@NonNull Application application) {
    this(application,
         new ChatRepository(application),
         new SessionManager(application),
         UUID::randomUUID);
}

ChatViewModel(Application application,
              ChatRepositoryContract repository,
              SessionManager session,
              Supplier<UUID> uuidSupplier) {
    super(application);
    this.repository = repository;
    this.session = session;
    this.uuidSupplier = uuidSupplier;
}
```

Maintain:

```java
private long operationGeneration;
private boolean sendInFlight;
private String selectedConversationId;
private String retryText;
private String retryRequestId;
```

Every async callback captures `long operation = operationGeneration`. Ignore it
when `operation != operationGeneration`. `startNewConversation()` increments the
generation, clears selection/messages/retry data, and sets `sendInFlight=false`.

- [ ] **Step 4: Implement send and retry transitions**

`send` must:

1. trim;
2. reject blank or over 1,000 chars through `ERROR`;
3. return if `sendInFlight`;
4. generate one UUID;
5. set `sending=true`;
6. call repository;
7. on success replace selected conversation/messages from response, clear retry,
   set sending false, refresh history, emit `SCROLL_TO_BOTTOM`;
8. on failure preserve text/request id, set sending false, emit mapped error;
9. on 401 emit `SESSION_EXPIRED`.

`retryFailed` calls the same private send method with `retryRequestId`, never a
new UUID.

- [ ] **Step 5: Run ViewModel tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatViewModelTest"
```

Expected: all ViewModel tests PASS.

### Task 3: Add the center navigation destination and visual resources

**Files:**

- Create: `app/src/main/res/navigation/nav_chat.xml`
- Modify: `app/src/main/res/navigation/nav_main.xml`
- Modify: `app/src/main/res/menu/bottom_nav_menu.xml`
- Create/modify all resource files listed in the File Map.
- Test: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatMessageAdapterContractTest.java`

**Interfaces:**

- Produces destination graph id `@id/nav_chat` with start destination
  `@id/chatFragment`.
- Produces binding classes for fragment, four message rows, history sheet,
  history row, and options sheet.

- [ ] **Step 1: Add a failing resource contract test**

Read XML resources as text and assert:

```java
@Test
public void bottomNav_placesChatThird() throws Exception {
    String xml = resource("menu/bottom_nav_menu.xml");
    int orders = xml.indexOf("@id/nav_ordes");
    int chat = xml.indexOf("@id/nav_chat");
    int favorites = xml.indexOf("@id/nav_favorites");
    assertTrue(orders < chat);
    assertTrue(chat < favorites);
}

@Test
public void chatTitle_matchesApprovedCopy() throws Exception {
    String xml = resource("values/strings.xml");
    assertTrue(xml.contains(
            "<string name=\"chat_title\">Chat bot trợ lý NGBT</string>"));
}
```

- [ ] **Step 2: Run contract test and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatMessageAdapterContractTest"
```

Expected: FAIL because resources are absent.

- [ ] **Step 3: Add navigation**

`nav_chat.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_chat"
    app:startDestination="@id/chatFragment">
    <fragment
        android:id="@+id/chatFragment"
        android:name="com.example.fooddelivery.ui.chat.ChatFragment"
        android:label="@string/chat_title" />
</navigation>
```

Insert `<include app:graph="@navigation/nav_chat"/>` in `nav_main.xml`. Insert
this item between Orders and Favorites:

```xml
<item
    android:id="@id/nav_chat"
    android:icon="@drawable/ic_chat_assistant"
    android:title="@string/nav_chat" />
```

- [ ] **Step 4: Add exact Vietnamese copy**

Add:

```xml
<string name="nav_chat">Trợ lý AI</string>
<string name="chat_title">Chat bot trợ lý NGBT</string>
<string name="chat_hint">Nhập câu hỏi về món ăn hoặc đơn hàng…</string>
<string name="chat_send">Gửi</string>
<string name="chat_history">Lịch sử trò chuyện</string>
<string name="chat_new">Cuộc trò chuyện mới</string>
<string name="chat_typing">Đang trả lời…</string>
<string name="chat_retry">Thử lại</string>
<string name="chat_helpful">Hữu ích</string>
<string name="chat_not_helpful">Không hữu ích</string>
<string name="chat_empty_title">Bạn muốn ăn gì hôm nay?</string>
<string name="chat_empty_suggestion_food">Gợi ý món ăn phù hợp với tôi</string>
<string name="chat_empty_suggestion_order">Đơn hàng gần nhất của tôi thế nào?</string>
<string name="chat_rename">Đổi tên</string>
<string name="chat_delete">Xóa cuộc trò chuyện</string>
<string name="chat_delete_confirm">Bạn chắc chắn muốn xóa cuộc trò chuyện này?</string>
```

Add direct English equivalents in `values-en/strings.xml`.

```xml
<string name="nav_chat">AI Assistant</string>
<string name="chat_title">NGBT Assistant Chatbot</string>
<string name="chat_hint">Ask about food or your order…</string>
<string name="chat_send">Send</string>
<string name="chat_history">Chat history</string>
<string name="chat_new">New conversation</string>
<string name="chat_typing">Replying…</string>
<string name="chat_retry">Retry</string>
<string name="chat_helpful">Helpful</string>
<string name="chat_not_helpful">Not helpful</string>
<string name="chat_empty_title">What would you like to eat today?</string>
<string name="chat_empty_suggestion_food">Suggest a suitable meal</string>
<string name="chat_empty_suggestion_order">What is the status of my latest order?</string>
<string name="chat_rename">Rename</string>
<string name="chat_delete">Delete conversation</string>
<string name="chat_delete_confirm">Are you sure you want to delete this conversation?</string>
```

- [ ] **Step 5: Create the main layout**

`fragment_chat.xml` must be a vertical layout with:

- 56dp toolbar containing title, history button, and new-chat button;
- empty-state container centered above the composer;
- `RecyclerView` with `layout_weight=1`, `clipToPadding=false`, and bottom padding;
- retry row hidden by default;
- composer containing a max-1,000-character multiline `EditText`, progress
  indicator, and send `ImageButton`;
- all dimensions in dp/sp resources or explicit values, never hard-coded pixels.

Use approved colors:

```xml
<color name="chat_user_bubble">#F5A623</color>
<color name="chat_assistant_bubble">#FFFFFF</color>
<color name="chat_screen_background">#FAFAFA</color>
<color name="chat_feedback_selected">#C47D0E</color>
```

- [ ] **Step 6: Create row/sheet layouts and vector assets**

Requirements:

- user bubble aligned end, white text on brand orange;
- assistant bubble aligned start, dark text on white, feedback row below;
- day row centered using secondary text;
- typing row uses the assistant bubble style;
- all icon buttons have content descriptions;
- history row has title, timestamp, and options button;
- options sheet exposes rename and delete;
- touch targets are at least 48dp.

- [ ] **Step 7: Run resource contract and assemble**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatMessageAdapterContractTest"
.\gradlew.bat :app:assembleDebug
```

Expected: contract tests PASS and resource linking/build succeeds.

### Task 4: Implement message/history adapters and bottom sheets

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatMessageAdapter.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatHistoryAdapter.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java`
- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatOptionsBottomSheet.java`
- Modify: `app/src/test/java/com/example/fooddelivery/ui/chat/ChatMessageAdapterContractTest.java`

**Interfaces:**

- `ChatMessageAdapter` consumes `List<ChatRow>`, where `ChatRow` is one of
  `DayRow`, `UserRow`, `AssistantRow`, or `TypingRow`.
- `ChatHistoryAdapter.Listener` exposes `onOpen`, `onOptions`.
- Bottom sheets communicate through listener interfaces; they never call the
  repository directly.

- [ ] **Step 1: Write failing adapter transformation tests**

Test pure `ChatRowFactory.from(messages, sending)`:

- inserts one day row when adjacent messages share a local date;
- inserts a new day row when date changes;
- preserves server order;
- appends exactly one typing row when sending;
- creates feedback controls only for assistant rows.

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatMessageAdapterContractTest"
```

Expected: transformation tests FAIL.

- [ ] **Step 3: Implement `ChatRow` and `ChatRowFactory`**

Create stable IDs:

```java
public long stableId() {
    if (this instanceof UserRow) return ((UserRow) this).message.getId() * 10 + 1;
    if (this instanceof AssistantRow) return ((AssistantRow) this).message.getId() * 10 + 2;
    if (this instanceof TypingRow) return Long.MAX_VALUE;
    return ((DayRow) this).epochDay * 10;
}
```

Parse Supabase ISO timestamps with `Instant.parse`, then convert using the device
zone for display. If parsing fails, keep the message and omit only the day label
instead of crashing.

- [ ] **Step 4: Implement adapters with generated bindings**

Use `ListAdapter<ChatRow, RecyclerView.ViewHolder>` and `DiffUtil.ItemCallback`.
Bind feedback selection from a message-id-to-value map. Do not call
`notifyDataSetChanged`; submit immutable lists.

- [ ] **Step 5: Implement history/options sheets**

`ChatHistoryBottomSheet` observes the activity/parent-fragment ViewModel and
submits history. Selecting a row calls `selectConversation(id)` and dismisses.
Options sheet validates renamed titles after trim: 1–120 characters. Delete
requires a Material confirmation dialog before invoking the ViewModel.

- [ ] **Step 6: Run adapter/ViewModel tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ChatMessageAdapterContractTest" --tests "*ChatViewModelTest"
```

Expected: all selected tests PASS.

### Task 5: Bind `ChatFragment` and complete interaction behavior

**Files:**

- Create: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/chat/ChatOptionsBottomSheet.java`

**Interfaces:**

- Consumes: `ChatViewModel`, generated bindings, adapters.
- Produces: complete screen behavior with no direct network calls.

- [ ] **Step 1: Implement binding lifecycle**

Use:

```java
private FragmentChatBinding binding;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    binding = FragmentChatBinding.inflate(inflater, container, false);
    return binding.getRoot();
}

@Override
public void onDestroyView() {
    binding.recyclerMessages.setAdapter(null);
    binding = null;
    super.onDestroyView();
}
```

- [ ] **Step 2: Bind interactions**

- send button and IME action call `viewModel.send(editText.getText().toString())`;
- successful send clears the editor only after the ViewModel accepts the send;
- suggestion chips copy their text and send;
- history/new buttons call their corresponding ViewModel/sheet actions;
- retry button calls `retryFailed`;
- feedback buttons call `setFeedback(message, 1/-1)`;
- text watcher disables send for blank, over-limit, or sending state.

- [ ] **Step 3: Render all states**

On each `ChatUiState`:

- submit `ChatRowFactory.from(messages, sending)`;
- show empty state only when messages are empty and not sending;
- show retry row only when retry data exists and not sending;
- swap send/progress visibility;
- disable feedback/history mutations while their operation is running;
- preserve draft text across view recreation with `onSaveInstanceState`.

Handle events once:

- `SCROLL_TO_BOTTOM` → `scrollToPosition(adapter.getItemCount() - 1)`;
- `ERROR` → Snackbar;
- `SESSION_EXPIRED` → clear session and navigate to the existing login destination;
- `CONVERSATION_DELETED` → dismiss sheets and show Snackbar.

- [ ] **Step 4: Add targeted Fragment contract checks**

Extend the XML/Java contract test to assert:

- `ChatFragment` nulls binding in `onDestroyView`;
- `ChatFragment` does not reference `ChatApiService` or `SupabaseClient`;
- `fragment_chat.xml` uses a RecyclerView and max length 1,000;
- title uses `@string/chat_title`.

- [ ] **Step 5: Run focused tests and build**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*Chat*"
.\gradlew.bat :app:assembleDebug
```

Expected: all chat tests PASS and debug APK builds.

### Task 6: Verify the full student-project flow

**Files:**

- Modify only if verification exposes a chatbot defect.

**Interfaces:**

- Consumes: deployed backend from the backend plan and an authenticated test user.
- Produces: evidence that frontend/backend work together without exposing secrets.

- [ ] **Step 1: Run the complete Android unit suite**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: all existing and chatbot unit tests PASS.

- [ ] **Step 2: Build and install**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Expected: build succeeds and ADB reports `Success`.

- [ ] **Step 3: Verify navigation and normal chat**

On the emulator:

1. log in;
2. confirm five bottom items and AI Assistant in the center;
3. open it and confirm title `Chat bot trợ lý NGBT`;
4. ask for a food suggestion and verify the response references a real Supabase menu row;
5. ask for the latest order and verify it matches the logged-in account;
6. background/rotate the app during a request and verify no duplicate send;
7. switch to a new conversation before an old callback completes and verify stale content does not replace it.

- [ ] **Step 4: Verify history, feedback, and failures**

1. reopen, rename, and delete a conversation;
2. submit helpful then not-helpful feedback and verify one upserted row;
3. disable network and verify retry appears;
4. restore network and retry with no duplicate user message;
5. force 401 and verify login routing;
6. exhaust the test daily limit and verify the Vietnamese 429 copy;
7. log in as a second user and verify the first user's history is absent.

- [ ] **Step 5: Inspect for leaked secrets**

Run:

```powershell
rg -n "OPENAI_API_KEY|SUPABASE_SERVICE_ROLE|sk-" app docs supabase --glob "!docs/superpowers/plans/*.md"
```

Expected: no key values and no Android references to server-only secret names.

- [ ] **Step 6: Record final evidence**

Report:

- total Android tests and failures;
- backend Deno test result;
- `assembleDebug` result;
- RLS two-user test result;
- manual emulator scenarios completed;
- deployment or live-key steps still requiring the user's Supabase/OpenAI access.
