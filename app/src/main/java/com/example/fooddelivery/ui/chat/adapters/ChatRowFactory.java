package com.example.fooddelivery.ui.chat.adapters;

import com.example.fooddelivery.data.model.chat.ChatMessage;
import com.example.fooddelivery.ui.chat.ChatUiState;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChatRowFactory {
    public List<ChatRow> create(ChatUiState state, ZoneId zoneId) {
        List<ChatRow> rows = new ArrayList<>();
        LocalDate previous = null;
        for (ChatMessage message : state.messages()) {
            LocalDate day = parseDay(message.getCreatedAt(), zoneId);
            if (day != null && !day.equals(previous)) {
                rows.add(new ChatRow.Day(day.toEpochDay() * 10,
                        day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))));
                previous = day;
            }
            if ("assistant".equals(message.getRole())) {
                rows.add(new ChatRow.Assistant(message, state.feedback().get(message.getId())));
            } else {
                rows.add(new ChatRow.Customer(message));
            }
        }
        if (state.sending() && state.pendingText() != null) {
            rows.add(new ChatRow.PendingCustomer(state.pendingText()));
            rows.add(new ChatRow.Typing());
        } else if (state.failedText() != null) {
            rows.add(new ChatRow.PendingCustomer(state.failedText()));
            rows.add(new ChatRow.Failed(state.failedText(), state.errorMessage()));
        }
        return rows;
    }

    private LocalDate parseDay(String value, ZoneId zoneId) {
        if (value == null) return null;
        try { return Instant.parse(value).atZone(zoneId).toLocalDate(); }
        catch (RuntimeException ignored) { return null; }
    }
}
