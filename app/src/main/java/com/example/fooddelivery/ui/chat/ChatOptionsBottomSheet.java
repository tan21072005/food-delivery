package com.example.fooddelivery.ui.chat;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.chat.ChatConversation;
import com.example.fooddelivery.databinding.BottomSheetChatOptionsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class ChatOptionsBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetChatOptionsBinding binding;
    private ChatConversation conversation;

    public static ChatOptionsBottomSheet newInstance(ChatConversation value) {
        ChatOptionsBottomSheet sheet = new ChatOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString("id", value.getId());
        args.putString("title", value.getTitle());
        args.putString("created", value.getCreatedAt());
        args.putString("updated", value.getUpdatedAt());
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle state) {
        binding = BottomSheetChatOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        Bundle args = requireArguments();
        conversation = new ChatConversation(args.getString("id"), args.getString("title"),
                args.getString("created"), args.getString("updated"));
        ChatViewModel vm = ((ChatFragment) requireParentFragment()).sharedViewModel();
        binding.renameConversationButton.setOnClickListener(v -> showRename(vm));
        binding.deleteConversationButton.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.chat_delete_confirm)
                        .setNegativeButton(R.string.btn_cancel, null)
                        .setPositiveButton(R.string.chat_delete, (dialog, which) -> {
                            vm.deleteConversation(conversation);
                            dismiss();
                        }).show());
    }

    private void showRename(ChatViewModel vm) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.chat_rename_hint);
        input.setText(conversation.getTitle());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chat_rename)
                .setView(input)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.chat_rename, (dialog, which) -> {
                    vm.renameConversation(conversation, input.getText().toString());
                    dismiss();
                }).show();
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
