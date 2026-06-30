package com.example.fooddelivery.data.repository;

public final class ChatFailure {
    private final int statusCode;
    private final String code;
    private final String userMessage;

    public ChatFailure(int statusCode, String code, String userMessage) {
        this.statusCode = statusCode;
        this.code = code;
        this.userMessage = userMessage;
    }

    public int statusCode() {
        return statusCode;
    }

    public String code() {
        return code;
    }

    public String userMessage() {
        return userMessage;
    }
}
