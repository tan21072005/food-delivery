package com.example.fooddelivery.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.model.chat.ChatSendRequest;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ChatApiService;
import com.example.fooddelivery.data.remote.response.ChatApiError;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ChatRepository implements ChatRepositoryContract {
    private static final String TAG = "ChatRepository";
    private static final String CONVERSATION_SELECT = "id,title,created_at,updated_at";
    private static final String MESSAGE_SELECT =
            "id,conversation_id,role,content,status,created_at";
    private final ChatApiService api;
    private final Gson gson = new Gson();

    public ChatRepository(Context context) {
        this(SupabaseClient.getInstance(context).create(ChatApiService.class));
    }

    public ChatRepository(ChatApiService api) {
        this.api = api;
    }

    @Override
    public void sendMessage(String conversationId, String text, String requestId,
                            ResultCallback<ChatSendResponse> callback) {
        api.sendMessage(new ChatSendRequest(conversationId, text, requestId))
                .enqueue(new Callback<ChatSendResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatSendResponse> call,
                                           @NonNull Response<ChatSendResponse> response) {
                        ChatSendResponse body = response.body();
                        if (response.isSuccessful() && body != null
                                && body.getAssistantMessage() != null) {
                            callback.onSuccess(body);
                        } else if (response.isSuccessful()) {
                            callback.onError(emptyResponse(response.code()));
                        } else {
                            callback.onError(mapError(response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatSendResponse> call,
                                          @NonNull Throwable throwable) {
                        callback.onError(networkError());
                    }
                });
    }

    @Override
    public void loadConversations(ResultCallback<List<ChatConversation>> callback) {
        api.getConversations(CONVERSATION_SELECT, "updated_at.desc", 50)
                .enqueue(listCallback(callback));
    }

    @Override
    public void loadMessages(String conversationId,
                             ResultCallback<List<ChatMessage>> callback) {
        api.getMessages(
                        "eq." + conversationId,
                        MESSAGE_SELECT,
                        "created_at.asc,id.asc")
                .enqueue(listCallback(callback));
    }

    @Override
    public void renameConversation(String conversationId, String title,
                                   ResultCallback<ChatConversation> callback) {
        api.renameConversation(
                        "eq." + conversationId,
                        Collections.singletonMap("title", title))
                .enqueue(singleItemCallback(callback));
    }

    @Override
    public void deleteConversation(String conversationId,
                                   ResultCallback<Void> callback) {
        api.deleteConversation("eq." + conversationId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call,
                                           @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(mapError(response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call,
                                          @NonNull Throwable throwable) {
                        callback.onError(networkError());
                    }
                });
    }

    @Override
    public void setFeedback(long messageId, int value, ResultCallback<ChatFeedback> callback) {
        if (value != -1 && value != 1) {
            callback.onError(new ChatFailure(
                    0,
                    "INVALID_FEEDBACK",
                    "Đánh giá không hợp lệ"));
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message_id", messageId);
        body.put("value", value);
        api.submitFeedback(body).enqueue(new Callback<ChatFeedback>() {
            @Override
            public void onResponse(@NonNull Call<ChatFeedback> call,
                                   @NonNull Response<ChatFeedback> response) {
                ChatFeedback body = response.body();
                if (response.isSuccessful() && body != null) {
                    callback.onSuccess(body);
                } else if (response.isSuccessful()) {
                    callback.onError(emptyResponse(response.code()));
                } else {
                    callback.onError(mapError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatFeedback> call,
                                  @NonNull Throwable throwable) {
                callback.onError(networkError());
            }
        });
    }

    private <T> Callback<List<T>> listCallback(ResultCallback<List<T>> callback) {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(@NonNull Call<List<T>> call,
                                   @NonNull Response<List<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onError(emptyResponse(response.code()));
                } else {
                    callback.onError(mapError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<T>> call,
                                  @NonNull Throwable throwable) {
                callback.onError(networkError());
            }
        };
    }

    private <T> Callback<List<T>> singleItemCallback(ResultCallback<T> callback) {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(@NonNull Call<List<T>> call,
                                   @NonNull Response<List<T>> response) {
                List<T> body = response.body();
                if (response.isSuccessful() && body != null && !body.isEmpty()) {
                    callback.onSuccess(body.get(0));
                } else if (response.isSuccessful()) {
                    callback.onError(emptyResponse(response.code()));
                } else {
                    callback.onError(mapError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<T>> call,
                                  @NonNull Throwable throwable) {
                callback.onError(networkError());
            }
        };
    }

    ChatFailure mapError(Response<?> response) {
        String body = "";
        if (response.errorBody() != null) {
            try {
                body = response.errorBody().string();
            } catch (IOException ignored) {
                body = "";
            }
        }
        Log.e(TAG, "Chat API failed. status=" + response.code() + ", body=" + body);
        return mapError(response.code(), body);
    }

    ChatFailure mapError(int statusCode, String json) {
        String code = "HTTP_ERROR";
        try {
            ChatApiError parsed = gson.fromJson(json, ChatApiError.class);
            if (parsed != null && parsed.getError() != null
                    && parsed.getError().getCode() != null) {
                code = parsed.getError().getCode();
            }
        } catch (RuntimeException ignored) {
            code = "HTTP_ERROR";
        }

        if (statusCode == 401) {
            return new ChatFailure(statusCode, code, "Phiên đăng nhập đã hết hạn");
        }
        if ("DAILY_LIMIT_REACHED".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Bạn đã dùng hết 15 lượt hỏi hôm nay");
        }
        if ("DATABASE_ERROR".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Không thể truy cập dữ liệu chat. Vui lòng thử lại");
        }
        if ("UPSTREAM_ERROR".equals(code) || "UPSTREAM_INVALID_RESPONSE".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Trợ lý AI chưa phản hồi được. Kiểm tra OpenAI key/model/quota");
        }
        if ("REQUEST_IN_PROGRESS".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "CĂ¢u há»i nĂ y Ä‘ang Ä‘Æ°á»£c xá»­ lĂ½. Vui lĂ²ng chá» trong giĂ¢y lĂ¡t");
        }
        if ("FORBIDDEN".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Báº¡n khĂ´ng cĂ³ quyá»n truy cáº­p cuá»™c trĂ² chuyá»‡n nĂ y");
        }
        if (code.startsWith("INVALID_")) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Dá»¯ liá»‡u chat khĂ´ng há»£p lá»‡. Vui lĂ²ng thá»­ láº¡i");
        }
        if ("UPSTREAM_RATE_LIMIT".equals(code)) {
            return new ChatFailure(
                    statusCode,
                    code,
                    "Trợ lý đang bận. Vui lòng thử lại sau");
        }
        return new ChatFailure(
                statusCode,
                code,
                "Không thể xử lý yêu cầu. Vui lòng thử lại");
    }

    private ChatFailure networkError() {
        return new ChatFailure(
                0,
                "NETWORK_ERROR",
                "Không thể kết nối. Vui lòng kiểm tra mạng");
    }

    private ChatFailure emptyResponse(int statusCode) {
        return new ChatFailure(
                statusCode,
                "EMPTY_RESPONSE",
                "Không thể xử lý yêu cầu. Vui lòng thử lại");
    }
}
