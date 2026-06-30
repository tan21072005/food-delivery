package com.example.fooddelivery.ui.chat;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatFragmentContractTest {
    @Test
    public void fragmentBindsApprovedChatFlow() throws Exception {
        String source = read("src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java");

        assertTrue(source.contains("binding = FragmentChatBinding.inflate"));
        assertTrue(source.contains("binding = null"));
        assertTrue(source.contains("viewModel.updateDraft"));
        assertTrue(source.contains("viewModel.send()"));
        assertTrue(source.contains("ChatHistoryBottomSheet"));
        assertTrue(source.contains("ChatEvent.SESSION_EXPIRED"));
        assertTrue(source.contains("action_chat_history"));
        assertTrue(source.contains("action_chat_new"));
    }

    @Test
    public void sheetsUseParentChatViewModel() throws Exception {
        String history = read(
                "src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java");
        String options = read(
                "src/main/java/com/example/fooddelivery/ui/chat/ChatOptionsBottomSheet.java");
        String fragment = read("src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java");

        assertTrue(fragment.contains("ChatViewModel sharedViewModel()"));
        assertTrue(history.contains("((ChatFragment) requireParentFragment()).sharedViewModel()"));
        assertTrue(options.contains("((ChatFragment) requireParentFragment()).sharedViewModel()"));
        assertFalse(history.contains("new ViewModelProvider(requireParentFragment())"));
        assertFalse(options.contains("new ViewModelProvider(requireParentFragment())"));
    }

    @Test
    public void historySheetExpandsToSeventyPercentHeight() throws Exception {
        String history = read(
                "src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java");

        assertTrue(history.contains("onStart()"));
        assertTrue(history.contains("heightPixels * 0.7f"));
        assertTrue(history.contains("setLayoutParams(bottomSheet.getLayoutParams())"));
        assertTrue(history.contains("BottomSheetBehavior.STATE_EXPANDED"));
        assertTrue(history.contains("setPeekHeight(targetHeight)"));
    }

    @Test
    public void historySheetDetachesRecyclerAdapterOnDestroyView() throws Exception {
        String history = read(
                "src/main/java/com/example/fooddelivery/ui/chat/ChatHistoryBottomSheet.java");

        assertTrue(history.contains("binding.historyList.setAdapter(null)"));
        assertTrue(history.contains("binding = null"));
    }

    @Test
    public void historyRowsFormatUpdatedAtForDisplay() throws Exception {
        String adapter = read(
                "src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatHistoryAdapter.java");

        assertTrue(adapter.contains("formatUpdatedAt("));
        assertTrue(adapter.contains("R.string.chat_today"));
        assertFalse(adapter.contains("conversationTime.setText(item.getUpdatedAt())"));
    }

    @Test
    public void messageAdapterResetsRecycledRetryAndFeedbackState() throws Exception {
        String adapter = read(
                "src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatMessageAdapter.java");
        String assistantLayout = read("src/main/res/layout/item_chat_assistant.xml");

        assertTrue(assistantLayout.contains("@+id/feedbackActions"));
        assertTrue(adapter.contains("isComplete(item.message().getStatus())"));
        assertTrue(adapter.contains("feedbackActions.setVisibility"));
        assertTrue(adapter.contains("typingText.setOnClickListener(null)"));
        assertTrue(adapter.contains("typingText.setClickable(false)"));
    }

    @Test
    public void messageAdapterFormatsMessageTimeInDeviceZone() throws Exception {
        String adapter = read(
                "src/main/java/com/example/fooddelivery/ui/chat/adapters/ChatMessageAdapter.java");

        assertTrue(adapter.contains("Instant.parse(value)"));
        assertTrue(adapter.contains("ZoneId.systemDefault()"));
        assertTrue(adapter.contains("DateTimeFormatter.ofPattern(\"HH:mm\""));
    }

    @Test
    public void expiredSessionOpensAuthActivityInsteadOfNavigatingToSiblingGraph() throws Exception {
        String source = read("src/main/java/com/example/fooddelivery/ui/chat/ChatFragment.java");

        assertTrue(source.contains("new Intent(requireActivity(), AuthActivity.class)"));
        assertTrue(source.contains("session.clearSession()"));
        assertFalse(source.contains("navigate(R.id.loginFragment)"));
    }

    private static String read(String relativePath) throws Exception {
        return new String(
                Files.readAllBytes(Path.of(System.getProperty("user.dir"), relativePath)),
                StandardCharsets.UTF_8);
    }
}
