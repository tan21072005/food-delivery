package com.example.fooddelivery.ui.chat;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChatUiState {
    private final ChatConversation conversation;
    private final List<ChatConversation> conversations;
    private final List<ChatMessage> messages;
    private final String draft;
    private final boolean loadingHistory;
    private final boolean sending;
    private final String pendingText;
    private final String failedText;
    private final String failedRequestId;
    private final String errorMessage;
    private final Map<Long, Integer> feedback;

    private ChatUiState(ChatConversation conversation,
                        List<ChatConversation> conversations,
                        List<ChatMessage> messages,
                        String draft,
                        boolean loadingHistory,
                        boolean sending,
                        String pendingText,
                        String failedText,
                        String failedRequestId,
                        String errorMessage,
                        Map<Long, Integer> feedback) {
        this.conversation = conversation;
        this.conversations = immutableList(conversations);
        this.messages = immutableList(messages);
        this.draft = draft;
        this.loadingHistory = loadingHistory;
        this.sending = sending;
        this.pendingText = pendingText;
        this.failedText = failedText;
        this.failedRequestId = failedRequestId;
        this.errorMessage = errorMessage;
        this.feedback = Collections.unmodifiableMap(new LinkedHashMap<>(feedback));
    }

    public static ChatUiState initial() {
        return new ChatUiState(null, Collections.emptyList(), Collections.emptyList(),
                "", false, false, null, null, null, null, Collections.emptyMap());
    }

    private static <T> List<T> immutableList(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public ChatConversation conversation() { return conversation; }
    public List<ChatConversation> conversations() { return conversations; }
    public List<ChatMessage> messages() { return messages; }
    public String draft() { return draft; }
    public boolean loadingHistory() { return loadingHistory; }
    public boolean sending() { return sending; }
    public String pendingText() { return pendingText; }
    public String failedText() { return failedText; }
    public String failedRequestId() { return failedRequestId; }
    public String errorMessage() { return errorMessage; }
    public Map<Long, Integer> feedback() { return feedback; }

    ChatUiState withDraft(String value) {
        return copy(conversation, conversations, messages, value, loadingHistory, sending,
                pendingText, failedText, failedRequestId, errorMessage, feedback);
    }

    ChatUiState withSending(String text) {
        return copy(conversation, conversations, messages, "", loadingHistory, true,
                text, null, null, null, feedback);
    }

    ChatUiState withSendSuccess(ChatSendResponse value) {
        List<ChatMessage> updatedMessages = new ArrayList<>(messages);
        if (value.getUserMessage() != null) updatedMessages.add(value.getUserMessage());
        if (value.getAssistantMessage() != null) updatedMessages.add(value.getAssistantMessage());
        ChatConversation updatedConversation =
                value.getConversation() == null ? conversation : value.getConversation();
        List<ChatConversation> updatedConversations =
                replaceOrPrepend(conversations, updatedConversation);
        return copy(updatedConversation, updatedConversations, updatedMessages, draft,
                loadingHistory, false, null, null, null, null, feedback);
    }

    ChatUiState withSendFailure(String text, String requestId, String message) {
        return copy(conversation, conversations, messages, draft, loadingHistory, false,
                null, text, requestId, message, feedback);
    }

    ChatUiState withNewConversation() {
        return copy(null, conversations, Collections.emptyList(), "", loadingHistory, false,
                null, null, null, null, Collections.emptyMap());
    }

    ChatUiState withSelectedConversation(ChatConversation value) {
        return copy(value, conversations, Collections.emptyList(), "", loadingHistory, false,
                null, null, null, null, Collections.emptyMap());
    }

    ChatUiState withMessages(List<ChatMessage> values) {
        return copy(conversation, conversations, values, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, null, feedback);
    }

    ChatUiState withError(String message) {
        return copy(conversation, conversations, messages, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, message, feedback);
    }

    ChatUiState withHistoryLoading() {
        return copy(conversation, conversations, messages, draft, true, sending, pendingText,
                failedText, failedRequestId, null, feedback);
    }

    ChatUiState withConversations(List<ChatConversation> values) {
        return copy(conversation, values, messages, draft, false, sending, pendingText,
                failedText, failedRequestId, null, feedback);
    }

    ChatUiState withHistoryFailure(String message) {
        return copy(conversation, conversations, messages, draft, false, sending, pendingText,
                failedText, failedRequestId, message, feedback);
    }

    ChatUiState withFeedback(long messageId, int value) {
        Map<Long, Integer> updated = new LinkedHashMap<>(feedback);
        updated.put(messageId, value);
        return copy(conversation, conversations, messages, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, null, updated);
    }

    ChatUiState withRenamedConversation(ChatConversation renamed) {
        List<ChatConversation> updated = replaceOrPrepend(conversations, renamed);
        ChatConversation selected = conversation != null
                && conversation.getId().equals(renamed.getId()) ? renamed : conversation;
        return copy(selected, updated, messages, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, null, feedback);
    }

    ChatUiState withDeletedConversation(ChatConversation deleted) {
        List<ChatConversation> updated = new ArrayList<>();
        for (ChatConversation item : conversations) {
            if (!deleted.getId().equals(item.getId())) updated.add(item);
        }
        if (conversation != null && conversation.getId().equals(deleted.getId())) {
            return copy(null, updated, Collections.emptyList(), "", loadingHistory, false,
                    null, null, null, null, Collections.emptyMap());
        }
        return copy(conversation, updated, messages, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, null, feedback);
    }

    ChatUiState withoutError() {
        return copy(conversation, conversations, messages, draft, loadingHistory, sending,
                pendingText, failedText, failedRequestId, null, feedback);
    }

    private ChatUiState copy(ChatConversation selected,
                             List<ChatConversation> conversationList,
                             List<ChatMessage> messageList,
                             String currentDraft,
                             boolean loading,
                             boolean isSending,
                             String pending,
                             String failed,
                             String failedId,
                             String error,
                             Map<Long, Integer> feedbackValues) {
        return new ChatUiState(selected, conversationList, messageList, currentDraft, loading,
                isSending, pending, failed, failedId, error, feedbackValues);
    }

    private static List<ChatConversation> replaceOrPrepend(
            List<ChatConversation> current, ChatConversation value) {
        if (value == null) return current;
        List<ChatConversation> result = new ArrayList<>();
        result.add(value);
        for (ChatConversation item : current) {
            if (!value.getId().equals(item.getId())) result.add(item);
        }
        return result;
    }
}
