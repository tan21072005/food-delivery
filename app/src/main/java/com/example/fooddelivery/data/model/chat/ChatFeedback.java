package com.example.fooddelivery.data.model.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatFeedback {
    private long id;
    @SerializedName("message_id")
    private final long messageId;
    @SerializedName("user_id")
    private final long userId;
    private final int value;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public ChatFeedback(long messageId, long userId, int value) {
        this.messageId = messageId;
        this.userId = userId;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getUserId() {
        return userId;
    }

    public int getValue() {
        return value;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
