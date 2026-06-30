package com.example.fooddelivery.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.remote.apis.ChatApiService;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatRepositoryTest {
    private static final String REQUEST_ID = "9d5833be-f943-41fc-9c2a-ded2528454e7";
    private static final String CONVERSATION_ID = "f083ea9f-4621-4d97-bae0-5676c4fcecd9";
    private MockWebServer server;
    private ChatRepository repository;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        ChatApiService api = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChatApiService.class);
        repository = new ChatRepository(api);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void sendMessage_postsEdgeFunctionContract() throws Exception {
        server.enqueue(json(200,
                "{\"conversation\":{\"id\":\"" + CONVERSATION_ID + "\",\"title\":\"Gợi ý món\"},"
                        + "\"user_message\":{\"id\":1,\"conversation_id\":\"" + CONVERSATION_ID
                        + "\",\"role\":\"user\",\"content\":\"Gợi ý món trưa\",\"status\":\"complete\"},"
                        + "\"assistant_message\":{\"id\":2,\"conversation_id\":\"" + CONVERSATION_ID
                        + "\",\"role\":\"assistant\",\"content\":\"Thử phở bò\",\"status\":\"complete\"},"
                        + "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}"));
        AtomicReference<ChatSendResponse> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        repository.sendMessage(null, "Gợi ý món trưa", REQUEST_ID,
                callback(result, latch));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        RecordedRequest request = server.takeRequest();
        assertEquals("/functions/v1/chat-assistant", request.getPath());
        assertEquals("POST", request.getMethod());
        assertTrue(request.getBody().readUtf8()
                .contains("\"client_request_id\":\"" + REQUEST_ID + "\""));
        assertNotNull(result.get().getAssistantMessage());
    }

    @Test
    public void sendMessage_normalizesBlankConversationIdToNull() throws Exception {
        server.enqueue(json(200,
                "{\"conversation\":{\"id\":\"" + CONVERSATION_ID + "\",\"title\":\"New chat\"},"
                        + "\"user_message\":{\"id\":1,\"conversation_id\":\"" + CONVERSATION_ID
                        + "\",\"role\":\"user\",\"content\":\"xin chao\",\"status\":\"complete\"},"
                        + "\"assistant_message\":{\"id\":2,\"conversation_id\":\"" + CONVERSATION_ID
                        + "\",\"role\":\"assistant\",\"content\":\"Xin chào\",\"status\":\"complete\"},"
                        + "\"usage\":{\"input_tokens\":1,\"output_tokens\":1}}"));
        CountDownLatch latch = new CountDownLatch(1);

        repository.sendMessage("", "xin chao", REQUEST_ID,
                callback(new AtomicReference<>(), latch));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        String body = server.takeRequest().getBody().readUtf8();
        assertTrue(!body.contains("\"conversation_id\":\"\""));
    }

    @Test
    public void loadMessages_filtersConversationAndOrdersAscending() throws Exception {
        server.enqueue(json(200, "[]"));
        AtomicReference<List<ChatMessage>> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        repository.loadMessages(CONVERSATION_ID, callback(result, latch));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
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

    @Test
    public void databaseError_mapsToActionableMessage() {
        ChatFailure failure = repository.mapError(
                502,
                "{\"error\":{\"code\":\"DATABASE_ERROR\",\"message\":\"x\"}}");

        assertEquals("DATABASE_ERROR", failure.code());
        assertEquals("Không thể truy cập dữ liệu chat. Vui lòng thử lại",
                failure.userMessage());
    }

    @Test
    public void upstreamError_mapsToOpenAiSetupMessage() {
        ChatFailure failure = repository.mapError(
                502,
                "{\"error\":{\"code\":\"UPSTREAM_ERROR\",\"message\":\"x\"}}");

        assertEquals("UPSTREAM_ERROR", failure.code());
        assertEquals("Trợ lý AI chưa phản hồi được. Kiểm tra OpenAI key/model/quota",
                failure.userMessage());
    }

    @Test
    public void requestInProgress_mapsToRetryLaterMessage() {
        ChatFailure failure = repository.mapError(
                409,
                "{\"error\":{\"code\":\"REQUEST_IN_PROGRESS\",\"message\":\"x\"}}");

        assertEquals("REQUEST_IN_PROGRESS", failure.code());
        assertEquals("CĂ¢u há»i nĂ y Ä‘ang Ä‘Æ°á»£c xá»­ lĂ½. Vui lĂ²ng chá» trong giĂ¢y lĂ¡t",
                failure.userMessage());
    }

    @Test
    public void forbidden_mapsToConversationPermissionMessage() {
        ChatFailure failure = repository.mapError(
                403,
                "{\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"x\"}}");

        assertEquals("FORBIDDEN", failure.code());
        assertEquals("Báº¡n khĂ´ng cĂ³ quyá»n truy cáº­p cuá»™c trĂ² chuyá»‡n nĂ y",
                failure.userMessage());
    }

    @Test
    public void invalidInput_mapsToValidationMessage() {
        ChatFailure failure = repository.mapError(
                400,
                "{\"error\":{\"code\":\"INVALID_CONVERSATION_ID\",\"message\":\"x\"}}");

        assertEquals("INVALID_CONVERSATION_ID", failure.code());
        assertEquals("Dá»¯ liá»‡u chat khĂ´ng há»£p lá»‡. Vui lĂ²ng thá»­ láº¡i",
                failure.userMessage());
    }

    @Test
    public void setFeedback_postsEdgeFunctionWithoutPublicUserId() throws Exception {
        server.enqueue(json(200,
                "{\"id\":1,\"message_id\":2,\"user_id\":42,\"value\":1,"
                        + "\"created_at\":\"2026-06-28T10:00:00Z\","
                        + "\"updated_at\":\"2026-06-28T10:00:00Z\"}"));
        AtomicReference<ChatFeedback> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        repository.setFeedback(2L, 1, callback(result, latch));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        RecordedRequest request = server.takeRequest();
        assertEquals("/functions/v1/chat-assistant/feedback", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"message_id\":2"));
        assertTrue(body.contains("\"value\":1"));
        assertTrue(!body.contains("\"user_id\""));
        assertEquals(42L, result.get().getUserId());
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static <T> ChatRepositoryContract.ResultCallback<T> callback(
            AtomicReference<T> result,
            CountDownLatch latch
    ) {
        return new ChatRepositoryContract.ResultCallback<T>() {
            @Override
            public void onSuccess(T value) {
                result.set(value);
                latch.countDown();
            }

            @Override
            public void onError(ChatFailure failure) {
                latch.countDown();
            }
        };
    }
}
