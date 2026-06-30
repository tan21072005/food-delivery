package com.example.fooddelivery.data.remote.response;

public final class ChatApiError {
    private ErrorBody error;

    public ErrorBody getError() {
        return error;
    }

    public static final class ErrorBody {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
