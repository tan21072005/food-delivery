package com.example.fooddelivery.ui.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;
import com.example.fooddelivery.data.repository.ChatRepositoryContract;

import com.google.gson.Gson;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class ChatViewModelTest {
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void sendTrimsDraftAndReplacesPendingMessageWithPersistedMessages() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.updateDraft("  Gợi ý Món trưa  ");
        viewModel.send();

        assertEquals("Gợi ý Món trưa", repository.sentText);
        assertTrue(viewModel.state().getValue().sending());
        assertEquals("", viewModel.state().getValue().draft());

        repository.sendCallback.onSuccess(successResponse());

        assertFalse(viewModel.state().getValue().sending());
        assertEquals(2, viewModel.state().getValue().messages().size());
    }

    @Test
    public void retryReusesFailedRequestId() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");
        viewModel.updateDraft("Pizza");
        viewModel.send();
        repository.sendCallback.onError(
                new com.example.fooddelivery.data.repository.ChatFailure(
                        0, "NETWORK", "Không thể kết nối"));

        viewModel.retry();

        assertEquals(2, repository.sendCount);
        assertEquals("request-1", repository.sentRequestId);
        assertEquals("Pizza", repository.sentText);
        assertNull(viewModel.state().getValue().failedText());
        assertTrue(viewModel.state().getValue().sending());
    }

    @Test
    public void ignoresConcurrentSend() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");
        viewModel.updateDraft("Pizza");

        viewModel.send();
        viewModel.updateDraft("Phở");
        viewModel.send();

        assertEquals(1, repository.sendCount);
        assertEquals("Pizza", repository.sentText);
    }

    @Test
    public void newConversationInvalidatesOlderSendCallback() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");
        viewModel.updateDraft("Pizza");
        viewModel.send();
        ChatRepositoryContract.ResultCallback<ChatSendResponse> old = repository.sendCallback;

        viewModel.newConversation();
        old.onSuccess(successResponse());

        assertNull(viewModel.state().getValue().conversation());
        assertTrue(viewModel.state().getValue().messages().isEmpty());
    }

    @Test
    public void selectingConversationLoadsItsMessagesAndIgnoresOlderSelection() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");
        ChatConversation first = conversation("first", "First");
        ChatConversation second = conversation("second", "Second");

        viewModel.selectConversation(first);
        ChatRepositoryContract.ResultCallback<List<ChatMessage>> old =
                repository.loadMessagesCallback;
        viewModel.selectConversation(second);
        repository.loadMessagesCallback.onSuccess(
                java.util.Collections.singletonList(message(2, "second", "assistant", "New")));
        old.onSuccess(java.util.Collections.singletonList(
                message(1, "first", "assistant", "Old")));

        assertEquals("second", viewModel.state().getValue().conversation().getId());
        assertEquals("New", viewModel.state().getValue().messages().get(0).getContent());
    }

    @Test
    public void loadsConversationHistory() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.loadConversations();

        assertTrue(viewModel.state().getValue().loadingHistory());
        repository.loadConversationsCallback.onSuccess(
                java.util.Collections.singletonList(conversation("first", "First")));
        assertFalse(viewModel.state().getValue().loadingHistory());
        assertEquals("first", viewModel.state().getValue().conversations().get(0).getId());
    }

    @Test
    public void firstHistoryLoadOpensLatestConversationMessages() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.loadConversations();
        repository.loadConversationsCallback.onSuccess(java.util.Arrays.asList(
                conversation("latest", "Latest"),
                conversation("older", "Older")));

        assertEquals("latest", viewModel.state().getValue().conversation().getId());
        repository.loadMessagesCallback.onSuccess(java.util.Collections.singletonList(
                message(10, "latest", "assistant", "Welcome back")));

        assertEquals("Welcome back", viewModel.state().getValue().messages().get(0).getContent());
    }

    @Test
    public void explicitNewConversationPreventsHistoryReloadFromAutoOpeningLatest() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.newConversation();
        viewModel.loadConversations();
        repository.loadConversationsCallback.onSuccess(
                java.util.Collections.singletonList(conversation("latest", "Latest")));

        assertNull(viewModel.state().getValue().conversation());
        assertNull(repository.loadMessagesCallback);
    }

    @Test
    public void feedbackDoesNotRequireAndroidPublicUserIdAndUpdatesOnlyAfterSuccess() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.setFeedback(12L, 1);

        assertEquals(12L, repository.feedbackMessageId);
        assertEquals(1, repository.feedbackValue);
        assertFalse(viewModel.state().getValue().feedback().containsKey(12L));
        repository.feedbackCallback.onSuccess(new ChatFeedback(12L, 77L, 1));
        assertEquals(Integer.valueOf(1), viewModel.state().getValue().feedback().get(12L));
    }

    @Test
    public void deletingAnotherConversationDoesNotInvalidateCurrentSend() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.selectConversation(conversation("current", "Current"));
        repository.loadMessagesCallback.onSuccess(java.util.Collections.emptyList());
        viewModel.updateDraft("Pizza");
        viewModel.send();
        ChatRepositoryContract.ResultCallback<ChatSendResponse> sendCallback =
                repository.sendCallback;

        viewModel.deleteConversation(conversation("other", "Other"));
        repository.deleteCallback.onSuccess(null);
        sendCallback.onSuccess(successResponse());

        assertFalse(viewModel.state().getValue().sending());
        assertEquals(2, viewModel.state().getValue().messages().size());
    }

    @Test
    public void renameConversationRejectsTitlesLongerThanOneHundredTwentyCharacters() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.renameConversation(conversation("current", "Current"),
                "a".repeat(121));

        assertEquals(0, repository.renameCount);
        assertEquals("Tên cuộc trò chuyện phải từ 1 đến 120 ký tự",
                viewModel.state().getValue().errorMessage());
    }

    private static ChatSendResponse successResponse() {
        return new Gson().fromJson("{"
                + "\"conversation\":{\"id\":\"conversation-1\",\"title\":\"Lunch\","
                + "\"created_at\":\"2026-06-29T00:00:00Z\","
                + "\"updated_at\":\"2026-06-29T00:00:01Z\"},"
                + "\"user_message\":{\"id\":1,\"conversation_id\":\"conversation-1\","
                + "\"role\":\"user\",\"content\":\"Gợi ý Món trưa\",\"status\":\"completed\","
                + "\"created_at\":\"2026-06-29T00:00:00Z\"},"
                + "\"assistant_message\":{\"id\":2,\"conversation_id\":\"conversation-1\","
                + "\"role\":\"assistant\",\"content\":\"Cơm gà\",\"status\":\"completed\","
                + "\"created_at\":\"2026-06-29T00:00:01Z\"}"
                + "}", ChatSendResponse.class);
    }

    private static ChatConversation conversation(String id, String title) {
        return new ChatConversation(id, title, "2026-06-29T00:00:00Z",
                "2026-06-29T00:00:00Z");
    }

    private static ChatMessage message(long id, String conversationId,
                                       String role, String content) {
        return new ChatMessage(id, conversationId, role, content, "completed",
                "2026-06-29T00:00:00Z");
    }

    private static final class FakeRepository implements ChatRepositoryContract {
        private String sentText;
        private String sentRequestId;
        private int sendCount;
        private ResultCallback<ChatSendResponse> sendCallback;
        private ResultCallback<List<ChatMessage>> loadMessagesCallback;
        private ResultCallback<List<ChatConversation>> loadConversationsCallback;
        private long feedbackMessageId;
        private int feedbackValue;
        private ResultCallback<ChatFeedback> feedbackCallback;
        private int renameCount;
        private ResultCallback<Void> deleteCallback;

        @Override
        public void sendMessage(String conversationId, String text, String requestId,
                                ResultCallback<ChatSendResponse> callback) {
            sentText = text;
            sentRequestId = requestId;
            sendCount++;
            sendCallback = callback;
        }

        @Override
        public void loadConversations(ResultCallback<List<ChatConversation>> callback) {
            loadConversationsCallback = callback;
        }

        @Override
        public void loadMessages(String conversationId,
                                 ResultCallback<List<ChatMessage>> callback) {
            loadMessagesCallback = callback;
        }

        @Override
        public void renameConversation(String conversationId, String title,
                                       ResultCallback<ChatConversation> callback) {
            renameCount++;
        }

        @Override
        public void deleteConversation(String conversationId, ResultCallback<Void> callback) {
            deleteCallback = callback;
        }

        @Override
        public void setFeedback(long messageId, int value, ResultCallback<ChatFeedback> callback) {
            feedbackMessageId = messageId;
            feedbackValue = value;
            feedbackCallback = callback;
        }
    }
}
