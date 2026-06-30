package com.example.fooddelivery.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fooddelivery.data.remote.response.ChatSendResponse;
import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.repository.ChatFailure;
import com.example.fooddelivery.data.repository.ChatRepositoryContract;

import java.util.List;

interface RequestIdFactory {
    String create();
}

public final class ChatViewModel extends ViewModel {
    private final ChatRepositoryContract repository;
    private final RequestIdFactory requestIds;
    private final MutableLiveData<ChatUiState> state =
            new MutableLiveData<>(ChatUiState.initial());
    private final MutableLiveData<ChatEvent> events = new MutableLiveData<>();
    private long operationGeneration;
    private long historyGeneration;
    private boolean shouldOpenLatestConversation = true;

    public ChatViewModel(ChatRepositoryContract repository, RequestIdFactory requestIds) {
        this.repository = repository;
        this.requestIds = requestIds;
    }

    public LiveData<ChatUiState> state() {
        return state;
    }

    public LiveData<ChatEvent> events() {
        return events;
    }

    public void updateDraft(String value) {
        state.setValue(current().withDraft(value == null ? "" : value));
    }

    public void send() {
        ChatUiState current = current();
        String text = current.draft().trim();
        if (text.isEmpty() || text.length() > 1000 || current.sending()) return;
        send(text, requestIds.create());
    }

    public void retry() {
        ChatUiState current = current();
        if (current.sending() || current.failedText() == null
                || current.failedRequestId() == null) {
            return;
        }
        send(current.failedText(), current.failedRequestId());
    }

    public void newConversation() {
        shouldOpenLatestConversation = false;
        operationGeneration++;
        state.setValue(current().withNewConversation());
    }

    public void selectConversation(ChatConversation conversation) {
        if (conversation == null) return;
        shouldOpenLatestConversation = false;
        long generation = ++operationGeneration;
        state.setValue(current().withSelectedConversation(conversation));
        repository.loadMessages(conversation.getId(),
                new ChatRepositoryContract.ResultCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> value) {
                        if (generation != operationGeneration) return;
                        state.setValue(current().withMessages(value));
                    }

                    @Override
                    public void onError(ChatFailure failure) {
                        if (generation != operationGeneration) return;
                        handleFailure(failure);
                    }
                });
    }

    public void loadConversations() {
        long generation = ++historyGeneration;
        state.setValue(current().withHistoryLoading());
        repository.loadConversations(
                new ChatRepositoryContract.ResultCallback<List<ChatConversation>>() {
                    @Override
                    public void onSuccess(List<ChatConversation> value) {
                        if (generation != historyGeneration) return;
                        state.setValue(current().withConversations(value));
                        maybeOpenLatestConversation(value);
                    }

                    @Override
                    public void onError(ChatFailure failure) {
                        if (generation != historyGeneration) return;
                        if (failure.statusCode() == 401) {
                            events.setValue(ChatEvent.SESSION_EXPIRED);
                        }
                        state.setValue(current().withHistoryFailure(failure.userMessage()));
                    }
                });
    }

    public void setFeedback(long messageId, int value) {
        long generation = operationGeneration;
        repository.setFeedback(messageId, value,
                new ChatRepositoryContract.ResultCallback<ChatFeedback>() {
                    @Override
                    public void onSuccess(ChatFeedback ignored) {
                        if (generation != operationGeneration) return;
                        state.setValue(current().withFeedback(messageId, value));
                    }

                    @Override
                    public void onError(ChatFailure failure) {
                        if (generation != operationGeneration) return;
                        handleFailure(failure);
                    }
                });
    }

    public void renameConversation(ChatConversation target, String title) {
        if (target == null || title == null) return;
        String trimmedTitle = title.trim();
        if (trimmedTitle.isEmpty() || trimmedTitle.length() > 120) {
            state.setValue(current().withError("Tên cuộc trò chuyện phải từ 1 đến 120 ký tự"));
            return;
        }
        repository.renameConversation(target.getId(), trimmedTitle,
                new ChatRepositoryContract.ResultCallback<ChatConversation>() {
                    @Override public void onSuccess(ChatConversation value) {
                        state.setValue(current().withRenamedConversation(value));
                    }
                    @Override public void onError(ChatFailure failure) {
                        handleFailure(failure);
                    }
                });
    }

    public void deleteConversation(ChatConversation target) {
        if (target == null) return;
        boolean deletesSelectedConversation = current().conversation() != null
                && target.getId().equals(current().conversation().getId());
        repository.deleteConversation(target.getId(),
                new ChatRepositoryContract.ResultCallback<Void>() {
                    @Override public void onSuccess(Void ignored) {
                        shouldOpenLatestConversation = false;
                        if (deletesSelectedConversation) {
                            operationGeneration++;
                        }
                        state.setValue(current().withDeletedConversation(target));
                    }
                    @Override public void onError(ChatFailure failure) {
                        handleFailure(failure);
                    }
                });
    }

    public void consumeError() {
        if (current().errorMessage() != null) {
            state.setValue(current().withoutError());
        }
    }

    public void consumeEvent() {
        events.setValue(null);
    }

    private void send(String text, String requestId) {
        long generation = ++operationGeneration;
        ChatUiState current = current();
        String conversationId =
                current.conversation() == null ? null : current.conversation().getId();
        state.setValue(current.withSending(text));
        repository.sendMessage(conversationId, text, requestId,
                new ChatRepositoryContract.ResultCallback<ChatSendResponse>() {
                    @Override
                    public void onSuccess(ChatSendResponse value) {
                        if (generation != operationGeneration) return;
                        state.setValue(current().withSendSuccess(value));
                    }

                    @Override
                    public void onError(ChatFailure failure) {
                        if (generation != operationGeneration) return;
                        if (failure.statusCode() == 401) {
                            events.setValue(ChatEvent.SESSION_EXPIRED);
                        }
                        state.setValue(current().withSendFailure(
                                text, requestId, failure.userMessage()));
                    }
                });
    }

    private void handleFailure(ChatFailure failure) {
        if (failure.statusCode() == 401) {
            events.setValue(ChatEvent.SESSION_EXPIRED);
        }
        state.setValue(current().withError(failure.userMessage()));
    }

    private void maybeOpenLatestConversation(List<ChatConversation> conversations) {
        ChatUiState current = current();
        if (!shouldOpenLatestConversation
                || conversations == null
                || conversations.isEmpty()
                || current.conversation() != null
                || !current.messages().isEmpty()
                || current.sending()
                || current.failedText() != null
                || !current.draft().trim().isEmpty()) {
            return;
        }
        shouldOpenLatestConversation = false;
        selectConversation(conversations.get(0));
    }

    private ChatUiState current() {
        ChatUiState value = state.getValue();
        return value == null ? ChatUiState.initial() : value;
    }
}
