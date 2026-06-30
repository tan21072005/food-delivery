package com.example.fooddelivery.data.repository;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;

import java.util.List;

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

    void setFeedback(long messageId, int value, ResultCallback<ChatFeedback> callback);
}
