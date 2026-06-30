package com.example.fooddelivery.ui.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.databinding.ItemChatHistoryBinding;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ChatHistoryAdapter
        extends ListAdapter<ChatConversation, ChatHistoryAdapter.Holder> {
    public interface Listener {
        void onSelect(ChatConversation conversation);
        void onOptions(ChatConversation conversation);
    }
    private final Listener listener;
    public ChatHistoryAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ChatConversation>() {
            @Override public boolean areItemsTheSame(@NonNull ChatConversation a,
                                                     @NonNull ChatConversation b) {
                return a.getId().equals(b.getId());
            }
            @Override public boolean areContentsTheSame(@NonNull ChatConversation a,
                                                        @NonNull ChatConversation b) {
                return a.getTitle().equals(b.getTitle())
                        && String.valueOf(a.getUpdatedAt()).equals(String.valueOf(b.getUpdatedAt()));
            }
        });
        this.listener = listener;
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemChatHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        ChatConversation item = getItem(position);
        h.binding.conversationTitle.setText(item.getTitle());
        h.binding.conversationTime.setText(formatUpdatedAt(
                h.binding.getRoot().getContext(),
                item.getUpdatedAt()));
        h.binding.getRoot().setOnClickListener(v -> listener.onSelect(item));
        h.binding.conversationOptionsButton.setOnClickListener(v -> listener.onOptions(item));
    }

    private String formatUpdatedAt(Context context, String value) {
        if (value == null || value.trim().isEmpty()) return "";
        try {
            LocalDate date = Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (date.equals(LocalDate.now(ZoneId.systemDefault()))) {
                return context.getString(R.string.chat_today);
            }
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()));
        } catch (RuntimeException ignored) {
            return value.length() > 10 ? value.substring(0, 10) : value;
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemChatHistoryBinding binding;
        Holder(ItemChatHistoryBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
