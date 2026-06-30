package com.example.fooddelivery.ui.chat;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.databinding.BottomSheetChatHistoryBinding;
import com.example.fooddelivery.ui.chat.adapters.ChatHistoryAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class ChatHistoryBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetChatHistoryBinding binding;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle state) {
        binding = BottomSheetChatHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        ChatViewModel vm = ((ChatFragment) requireParentFragment()).sharedViewModel();
        ChatHistoryAdapter adapter = new ChatHistoryAdapter(new ChatHistoryAdapter.Listener() {
            @Override public void onSelect(ChatConversation conversation) {
                vm.selectConversation(conversation);
                dismiss();
            }
            @Override public void onOptions(ChatConversation conversation) {
                ChatOptionsBottomSheet.newInstance(conversation).show(
                        requireParentFragment().getChildFragmentManager(), "chat-options");
                dismiss();
            }
        });
        binding.historyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyList.setAdapter(adapter);
        binding.closeHistoryButton.setOnClickListener(v -> dismiss());
        binding.newConversationButton.setOnClickListener(v -> {
            vm.newConversation();
            dismiss();
        });
        vm.state().observe(getViewLifecycleOwner(),
                chatState -> adapter.submitList(chatState.conversations()));
        vm.loadConversations();
    }

    @Override public void onDestroyView() {
        if (binding != null) {
            binding.historyList.setAdapter(null);
        }
        binding = null;
        super.onDestroyView();
    }

    @Override public void onStart() {
        super.onStart();
        View bottomSheet = requireDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet instanceof FrameLayout) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int targetHeight = (int) (metrics.heightPixels * 0.7f);
            bottomSheet.getLayoutParams().height = targetHeight;
            bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            BottomSheetBehavior<FrameLayout> behavior =
                    BottomSheetBehavior.from((FrameLayout) bottomSheet);
            behavior.setPeekHeight(targetHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
