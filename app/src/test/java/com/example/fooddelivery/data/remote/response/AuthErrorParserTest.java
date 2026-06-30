package com.example.fooddelivery.data.remote.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class AuthErrorParserTest {

    @Test
    public void mapsCommonSupabaseSignupErrorsToVietnameseMessages() {
        assertEquals(
                "Email n\u00e0y \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u0103ng k\u00fd",
                AuthErrorParser.parse(Response.error(400, json("{\"msg\":\"User already registered\"}")))
        );
        assertEquals(
                "Email kh\u00f4ng h\u1ee3p l\u1ec7",
                AuthErrorParser.parse(Response.error(400, json("{\"message\":\"Unable to validate email address: invalid format\"}")))
        );
        assertEquals(
                "M\u1eadt kh\u1ea9u qu\u00e1 y\u1ebfu. Vui l\u00f2ng ch\u1ecdn m\u1eadt kh\u1ea9u m\u1ea1nh h\u01a1n",
                AuthErrorParser.parse(Response.error(422, json("{\"error_description\":\"Password should be at least 6 characters\"}")))
        );
        assertEquals(
                "B\u1ea1n thao t\u00e1c qu\u00e1 nhanh. Vui l\u00f2ng th\u1eed l\u1ea1i sau",
                AuthErrorParser.parse(Response.error(429, json("{\"error\":\"rate limit exceeded\"}")))
        );
    }

    @Test
    public void fallsBackByHttpStatusWhenBodyIsMissingOrUnknown() {
        assertEquals(
                "L\u1ed7i m\u00e1y ch\u1ee7. Vui l\u00f2ng th\u1eed l\u1ea1i sau",
                AuthErrorParser.parse(Response.error(500, json("{\"message\":\"unexpected\"}")))
        );
        assertEquals(
                "\u0110\u0103ng k\u00fd th\u1ea5t b\u1ea1i. Vui l\u00f2ng th\u1eed l\u1ea1i",
                AuthErrorParser.parse(Response.error(400, json("{\"code\":\"bad_json\"}")))
        );
    }

    private ResponseBody json(String body) {
        return ResponseBody.create(MediaType.get("application/json"), body);
    }
}
