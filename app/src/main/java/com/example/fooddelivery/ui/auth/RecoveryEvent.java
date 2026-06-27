package com.example.fooddelivery.ui.auth;

public final class RecoveryEvent {
    public enum Type { CODE_SENT, CODE_RESENT, OTP_VERIFIED, PASSWORD_UPDATED, ERROR }

    private final Type type;
    private final String message;
    private boolean handled;

    public RecoveryEvent(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public synchronized RecoveryEvent consume() {
        if (handled) return null;
        handled = true;
        return this;
    }

    public Type type() { return type; }
    public String message() { return message; }
}
