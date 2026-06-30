package com.example.fooddelivery.data.remote.apis;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.data.model.chat.ChatFeedback;
import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.data.model.chat.ChatSendRequest;
import com.example.fooddelivery.data.remote.response.ChatSendResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApiService {
    @POST("functions/v1/chat-assistant")
    Call<ChatSendResponse> sendMessage(@Body ChatSendRequest request);

    @GET("rest/v1/chat_conversations")
    Call<List<ChatConversation>> getConversations(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit);

    @GET("rest/v1/chat_messages")
    Call<List<ChatMessage>> getMessages(
            @Query("conversation_id") String conversationFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/chat_conversations")
    Call<List<ChatConversation>> renameConversation(
            @Query("id") String idFilter,
            @Body Map<String, String> body);

    @DELETE("rest/v1/chat_conversations")
    Call<Void> deleteConversation(@Query("id") String idFilter);

    @POST("functions/v1/chat-assistant/feedback")
    Call<ChatFeedback> submitFeedback(@Body Map<String, Object> feedback);
}
