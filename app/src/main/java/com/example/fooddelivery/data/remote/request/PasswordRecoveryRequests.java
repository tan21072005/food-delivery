package com.example.fooddelivery.data.remote.request;

import com.google.gson.annotations.SerializedName;

public final class PasswordRecoveryRequests {
    private PasswordRecoveryRequests() {
    }

    public static final class Email {
        @SerializedName("email")
        private final String email;

        public Email(String email) {
            this.email = email;
        }
    }

    public static final class VerifyOtp {
        @SerializedName("email")
        private final String email;
        @SerializedName("token")
        private final String token;
        @SerializedName("type")
        private final String type = "recovery";

        public VerifyOtp(String email, String token) {
            this.email = email;
            this.token = token;
        }
    }

    public static final class NewPassword {
        @SerializedName("password")
        private final String password;

        public NewPassword(String password) {
            this.password = password;
        }
    }
}
