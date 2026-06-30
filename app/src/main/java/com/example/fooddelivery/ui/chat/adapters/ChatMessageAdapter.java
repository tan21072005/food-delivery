package com.example.fooddelivery.ui.chat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.ItemChatAssistantBinding;
import com.example.fooddelivery.databinding.ItemChatDayBinding;
import com.example.fooddelivery.databinding.ItemChatTypingBinding;
import com.example.fooddelivery.databinding.ItemChatUserBinding;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ChatMessageAdapter
        extends ListAdapter<ChatRow, RecyclerView.ViewHolder> {
    public interface Listener {
        void onFeedback(long messageId, int value);
        void onRetry();
    }

    private static final int DAY = 0, CUSTOMER = 1, ASSISTANT = 2, TYPING = 3, FAILED = 4;
    private final Listener listener;

    public ChatMessageAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ChatRow>() {
            @Override public boolean areItemsTheSame(@NonNull ChatRow a, @NonNull ChatRow b) {
                return a.stableId() == b.stableId();
            }
            @Override public boolean areContentsTheSame(@NonNull ChatRow a, @NonNull ChatRow b) {
                return a.getClass() == b.getClass() && a.toString().equals(b.toString());
            }
        });
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override public long getItemId(int position) { return getItem(position).stableId(); }

    @Override public int getItemViewType(int position) {
        ChatRow row = getItem(position);
        if (row instanceof ChatRow.Day) return DAY;
        if (row instanceof ChatRow.Assistant) return ASSISTANT;
        if (row instanceof ChatRow.Typing) return TYPING;
        if (row instanceof ChatRow.Failed) return FAILED;
        return CUSTOMER;
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (type == DAY) return new Holder(ItemChatDayBinding.inflate(inflater, parent, false));
        if (type == ASSISTANT) return new Holder(ItemChatAssistantBinding.inflate(inflater, parent, false));
        if (type == TYPING || type == FAILED)
            return new Holder(ItemChatTypingBinding.inflate(inflater, parent, false));
        return new Holder(ItemChatUserBinding.inflate(inflater, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatRow row = getItem(position);
        Object binding = ((Holder) holder).binding;
        if (row instanceof ChatRow.Day) {
            ((ItemChatDayBinding) binding).dayLabel.setText(((ChatRow.Day) row).label());
        } else if (row instanceof ChatRow.Assistant) {
            ChatRow.Assistant item = (ChatRow.Assistant) row;
            ItemChatAssistantBinding b = (ItemChatAssistantBinding) binding;
            b.assistantMessageText.setText(item.message().getContent());
            b.assistantMessageTime.setText(time(item.message().getCreatedAt()));
            boolean feedbackVisible = isComplete(item.message().getStatus());
            b.feedbackPrompt.setVisibility(feedbackVisible ? View.VISIBLE : View.GONE);
            b.feedbackActions.setVisibility(feedbackVisible ? View.VISIBLE : View.GONE);
            b.feedbackUpButton.setStrokeColorResource(
                    Integer.valueOf(1).equals(item.feedback()) ? R.color.chat_orange : R.color.chat_composer_stroke);
            b.feedbackDownButton.setStrokeColorResource(
                    Integer.valueOf(-1).equals(item.feedback()) ? R.color.chat_orange : R.color.chat_composer_stroke);
            b.feedbackUpButton.setOnClickListener(v -> listener.onFeedback(item.message().getId(), 1));
            b.feedbackDownButton.setOnClickListener(v -> listener.onFeedback(item.message().getId(), -1));
        } else if (row instanceof ChatRow.Typing) {
            ItemChatTypingBinding b = (ItemChatTypingBinding) binding;
            b.typingText.setText(R.string.chat_typing);
            b.typingText.setOnClickListener(null);
            b.typingText.setClickable(false);
        } else if (row instanceof ChatRow.Failed) {
            ItemChatTypingBinding b = (ItemChatTypingBinding) binding;
            b.typingText.setText(R.string.chat_retry);
            b.typingText.setClickable(true);
            b.typingText.setOnClickListener(v -> listener.onRetry());
        } else {
            ItemChatUserBinding b = (ItemChatUserBinding) binding;
            if (row instanceof ChatRow.Customer) {
                b.userMessageText.setText(((ChatRow.Customer) row).message().getContent());
                b.userMessageTime.setText(time(((ChatRow.Customer) row).message().getCreatedAt()));
            } else {
                b.userMessageText.setText(((ChatRow.PendingCustomer) row).content());
                b.userMessageTime.setText("");
            }
        }
    }

    private String time(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        try {
            return Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
        } catch (RuntimeException ignored) {
            return value.length() >= 16 ? value.substring(11, 16) : "";
        }
    }

    private boolean isComplete(String status) {
        return "complete".equals(status) || "completed".equals(status);
    }

    private static final class Holder extends RecyclerView.ViewHolder {
        final Object binding;
        Holder(ItemChatDayBinding b) { super(b.getRoot()); binding = b; }
        Holder(ItemChatUserBinding b) { super(b.getRoot()); binding = b; }
        Holder(ItemChatAssistantBinding b) { super(b.getRoot()); binding = b; }
        Holder(ItemChatTypingBinding b) { super(b.getRoot()); binding = b; }
    }
}
