package com.example.fooddelivery.data.model.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatMessage {
    private long id;
    @SerializedName("conversation_id")
    private String conversationId;
    private String role;
    private String content;
    private String status;
    @SerializedName("created_at")
    private String createdAt;

    public ChatMessage(long id, String conversationId, String role, String content,
                       String status, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
