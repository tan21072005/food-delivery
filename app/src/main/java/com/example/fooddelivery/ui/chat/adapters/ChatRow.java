package com.example.fooddelivery.ui.chat.adapters;

import com.example.fooddelivery.data.model.chat.ChatMessage;

public abstract class ChatRow {
    public abstract long stableId();

    public static final class Day extends ChatRow {
        private final long id;
        private final String label;
        public Day(long id, String label) { this.id = id; this.label = label; }
        public String label() { return label; }
        @Override public long stableId() { return id; }
    }

    public static final class Customer extends ChatRow {
        private final ChatMessage message;
        public Customer(ChatMessage message) { this.message = message; }
        public ChatMessage message() { return message; }
        @Override public long stableId() { return message.getId() * 10 + 1; }
    }

    public static final class PendingCustomer extends ChatRow {
        private final String content;
        public PendingCustomer(String content) { this.content = content; }
        public String content() { return content; }
        @Override public long stableId() { return Long.MAX_VALUE - 3; }
    }

    public static final class Assistant extends ChatRow {
        private final ChatMessage message;
        private final Integer feedback;
        public Assistant(ChatMessage message, Integer feedback) {
            this.message = message; this.feedback = feedback;
        }
        public ChatMessage message() { return message; }
        public Integer feedback() { return feedback; }
        @Override public long stableId() { return message.getId() * 10 + 2; }
    }

    public static final class Typing extends ChatRow {
        @Override public long stableId() { return Long.MAX_VALUE - 1; }
    }

    public static final class Failed extends ChatRow {
        private final String content;
        private final String error;
        public Failed(String content, String error) { this.content = content; this.error = error; }
        public String content() { return content; }
        public String error() { return error; }
        @Override public long stableId() { return Long.MAX_VALUE - 2; }
    }
}
