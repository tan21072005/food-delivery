package com.example.fooddelivery.data.remote.response;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class AuthErrorParser {
    public static final String GENERIC_SIGNUP_ERROR =
            "\u0110\u0103ng k\u00fd th\u1ea5t b\u1ea1i. Vui l\u00f2ng th\u1eed l\u1ea1i";
    public static final String SERVER_ERROR =
            "L\u1ed7i m\u00e1y ch\u1ee7. Vui l\u00f2ng th\u1eed l\u1ea1i sau";
    public static final String NETWORK_ERROR_PREFIX =
            "L\u1ed7i k\u1ebft n\u1ed1i: ";
    public static final String EMAIL_CONFIRMATION_REQUIRED =
            "\u0110\u0103ng k\u00fd th\u00e0nh c\u00f4ng. Vui l\u00f2ng x\u00e1c nh\u1eadn email tr\u01b0\u1edbc khi \u0111\u0103ng nh\u1eadp";

    private AuthErrorParser() {
    }

    public static String parse(Response<AuthResponse> response) {
        if (response == null) {
            return GENERIC_SIGNUP_ERROR;
        }

        String bodyMessage = readBodyMessage(response.errorBody());
        String mappedMessage = mapMessage(bodyMessage, response.code());
        if (mappedMessage != null) {
            return mappedMessage;
        }

        if (response.code() >= 500) {
            return SERVER_ERROR;
        }

        return GENERIC_SIGNUP_ERROR;
    }

    public static String networkMessage(Throwable throwable) {
        String detail = throwable != null && throwable.getMessage() != null
                ? throwable.getMessage()
                : "";
        return NETWORK_ERROR_PREFIX + detail;
    }

    private static String readBodyMessage(ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }

        try {
            String body = errorBody.string();
            JsonObject json = new JsonParser().parse(body).getAsJsonObject();
            for (String field : new String[]{"msg", "message", "error_description", "error"}) {
                if (json.has(field) && !json.get(field).isJsonNull()) {
                    return json.get(field).getAsString();
                }
            }
        } catch (IOException | IllegalStateException ignored) {
            return null;
        }
        return null;
    }

    private static String mapMessage(String message, int statusCode) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.US);

        if (statusCode == 429
                || normalized.contains("rate")
                || normalized.contains("too many")
                || normalized.contains("over_email_send_rate_limit")) {
            return "B\u1ea1n thao t\u00e1c qu\u00e1 nhanh. Vui l\u00f2ng th\u1eed l\u1ea1i sau";
        }

        if (normalized.contains("already registered")
                || normalized.contains("already exists")
                || normalized.contains("user already")
                || normalized.contains("email_exists")) {
            return "Email n\u00e0y \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u0103ng k\u00fd";
        }

        if (normalized.contains("invalid email")
                || normalized.contains("unable to validate email")
                || normalized.contains("invalid format")) {
            return "Email kh\u00f4ng h\u1ee3p l\u1ec7";
        }

        if (normalized.contains("password")
                && (normalized.contains("weak")
                || normalized.contains("at least")
                || normalized.contains("characters")
                || normalized.contains("should be"))) {
            return "M\u1eadt kh\u1ea9u qu\u00e1 y\u1ebfu. Vui l\u00f2ng ch\u1ecdn m\u1eadt kh\u1ea9u m\u1ea1nh h\u01a1n";
        }

        return null;
    }
}
