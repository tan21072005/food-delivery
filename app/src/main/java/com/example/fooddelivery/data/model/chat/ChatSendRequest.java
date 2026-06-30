package com.example.fooddelivery.data.model.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatSendRequest {
    @SerializedName("conversation_id")
    private final String conversationId;
    private final String message;
    @SerializedName("client_request_id")
    private final String clientRequestId;

    public ChatSendRequest(String conversationId, String message, String clientRequestId) {
        this.conversationId = normalizeConversationId(conversationId);
        this.message = message;
        this.clientRequestId = clientRequestId;
    }

    private static String normalizeConversationId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
