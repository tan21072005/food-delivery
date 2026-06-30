package com.example.fooddelivery.ui.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;
import com.example.fooddelivery.data.repository.ChatFailure;
import com.example.fooddelivery.data.repository.ChatRepositoryContract;
import com.example.fooddelivery.ui.chat.adapters.ChatRow;
import com.example.fooddelivery.ui.chat.adapters.ChatRowFactory;

import org.junit.Rule;
import org.junit.Test;

import java.time.ZoneId;
import java.util.List;

public class ChatRowFactoryTest {
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void failedSendKeepsQuestionVisibleBeforeRetryRow() {
        FakeRepository repository = new FakeRepository();
        ChatViewModel viewModel = new ChatViewModel(repository, () -> "request-1");

        viewModel.updateDraft("Gợi ý món trưa");
        viewModel.send();
        repository.sendCallback.onError(new ChatFailure(
                0, "NETWORK", "Không thể kết nối. Vui lòng kiểm tra mạng"));

        List<ChatRow> rows = new ChatRowFactory().create(
                viewModel.state().getValue(), ZoneId.of("Asia/Ho_Chi_Minh"));

        assertEquals(2, rows.size());
        assertTrue(rows.get(0) instanceof ChatRow.PendingCustomer);
        assertTrue(rows.get(1) instanceof ChatRow.Failed);
        assertEquals("Gợi ý món trưa", ((ChatRow.PendingCustomer) rows.get(0)).content());
    }

    private static final class FakeRepository implements ChatRepositoryContract {
        private ResultCallback<ChatSendResponse> sendCallback;

        @Override
        public void sendMessage(String conversationId, String text, String requestId,
                                ResultCallback<ChatSendResponse> callback) {
            sendCallback = callback;
        }

        @Override
        public void loadConversations(ResultCallback<List<ChatConversation>> callback) {}

        @Override
        public void loadMessages(String conversationId,
                                 ResultCallback<List<ChatMessage>> callback) {}

        @Override
        public void renameConversation(String conversationId, String title,
                                       ResultCallback<ChatConversation> callback) {}

        @Override
        public void deleteConversation(String conversationId, ResultCallback<Void> callback) {}

        @Override
        public void setFeedback(long messageId, int value,
                                ResultCallback<ChatFeedback> callback) {}
    }
}
