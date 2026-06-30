package com.example.fooddelivery.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.repository.ChatRepository;
import com.example.fooddelivery.databinding.FragmentChatBinding;
import com.example.fooddelivery.ui.auth.AuthActivity;
import com.example.fooddelivery.ui.chat.adapters.ChatMessageAdapter;
import com.example.fooddelivery.ui.chat.adapters.ChatRow;
import com.example.fooddelivery.ui.chat.adapters.ChatRowFactory;
import com.google.android.material.snackbar.Snackbar;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public final class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessageAdapter adapter;
    private SessionManager session;
    private final ChatRowFactory rowFactory = new ChatRowFactory();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle state) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            openLogin();
            return;
        }
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends androidx.lifecycle.ViewModel> T create(
                    @NonNull Class<T> modelClass) {
                return modelClass.cast(new ChatViewModel(
                        new ChatRepository(requireContext()),
                        () -> UUID.randomUUID().toString()));
            }
        }).get(ChatViewModel.class);

        adapter = new ChatMessageAdapter(new ChatMessageAdapter.Listener() {
            @Override public void onFeedback(long messageId, int value) {
                viewModel.setFeedback(messageId, value);
            }
            @Override public void onRetry() { viewModel.retry(); }
        });
        binding.messageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.messageList.setAdapter(adapter);

        binding.chatToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_chat_history) {
                new ChatHistoryBottomSheet().show(
                        getChildFragmentManager(), "chat-history");
                return true;
            }
            if (item.getItemId() == R.id.action_chat_new) {
                viewModel.newConversation();
                return true;
            }
            return false;
        });
        binding.messageInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.updateDraft(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        binding.sendButton.setOnClickListener(v -> viewModel.send());
        binding.messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                viewModel.send();
                return true;
            }
            return false;
        });
        binding.lunchSuggestion.setOnClickListener(v -> setSuggestion(R.string.chat_suggestion_lunch));
        binding.restaurantSuggestion.setOnClickListener(
                v -> setSuggestion(R.string.chat_suggestion_restaurant));
        binding.orderSuggestion.setOnClickListener(v -> setSuggestion(R.string.chat_suggestion_order));

        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        viewModel.events().observe(getViewLifecycleOwner(), event -> {
            if (event == ChatEvent.SESSION_EXPIRED) {
                viewModel.consumeEvent();
                openLogin();
            }
        });
        viewModel.loadConversations();
    }

    private void setSuggestion(int stringId) {
        binding.messageInput.setText(stringId);
        binding.messageInput.setSelection(binding.messageInput.length());
    }

    private void openLogin() {
        session.clearSession();
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void render(ChatUiState state) {
        boolean empty = state.messages().isEmpty() && !state.sending()
                && state.failedText() == null;
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.messageList.setVisibility(empty ? View.GONE : View.VISIBLE);
        String trimmedDraft = state.draft().trim();
        binding.sendButton.setEnabled(!state.sending()
                && !trimmedDraft.isEmpty()
                && trimmedDraft.length() <= 1000);
        if (!binding.messageInput.getText().toString().equals(state.draft())) {
            binding.messageInput.setText(state.draft());
            binding.messageInput.setSelection(binding.messageInput.length());
        }
        List<ChatRow> rows = rowFactory.create(state, ZoneId.systemDefault());
        adapter.submitList(rows, () -> {
            if (!rows.isEmpty()) binding.messageList.scrollToPosition(rows.size() - 1);
        });
        if (state.errorMessage() != null) {
            Snackbar.make(binding.getRoot(), state.errorMessage(), Snackbar.LENGTH_LONG).show();
            viewModel.consumeError();
        }
    }

    ChatViewModel sharedViewModel() {
        return viewModel;
    }

    @Override public void onDestroyView() {
        if (binding != null) {
            binding.messageList.setAdapter(null);
        }
        binding = null;
        super.onDestroyView();
    }
}
