package com.example.fooddelivery.data.remote.response;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.google.gson.annotations.SerializedName;

public final class ChatSendResponse {
    private ChatConversation conversation;
    @SerializedName("user_message")
    private ChatMessage userMessage;
    @SerializedName("assistant_message")
    private ChatMessage assistantMessage;
    private Usage usage;

    public ChatConversation getConversation() {
        return conversation;
    }

    public ChatMessage getUserMessage() {
        return userMessage;
    }

    public ChatMessage getAssistantMessage() {
        return assistantMessage;
    }

    public Usage getUsage() {
        return usage;
    }

    public static final class Usage {
        @SerializedName("input_tokens")
        private int inputTokens;
        @SerializedName("output_tokens")
        private int outputTokens;

        public int getInputTokens() {
            return inputTokens;
        }

        public int getOutputTokens() {
            return outputTokens;
        }
    }
}
