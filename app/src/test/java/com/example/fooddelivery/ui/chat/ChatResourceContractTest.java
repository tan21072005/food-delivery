package com.example.fooddelivery.ui.chat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class ChatResourceContractTest {

    @Test
    public void chatbotIsTheThirdOrangeBottomNavigationDestination() throws IOException {
        String menu = readResource("menu/bottom_nav_menu.xml");
        String mainGraph = readResource("navigation/nav_main.xml");
        String chatGraph = readResource("navigation/nav_chat.xml");
        String toolbarMenu = readResource("menu/chat_toolbar_menu.xml");
        String strings = readResource("values/strings.xml");
        String colors = readResource("values/colors.xml");
        String fragmentLayout = readResource("layout/fragment_chat.xml");
        String activityLayout = readResource("layout/main_activity.xml");

        assertTrue(menu.indexOf("@id/nav_ordes") < menu.indexOf("@id/nav_chat"));
        assertTrue(menu.indexOf("@id/nav_chat") < menu.indexOf("@id/nav_favorites"));
        assertTrue(mainGraph.contains("@navigation/nav_chat"));
        assertTrue(chatGraph.contains("android:id=\"@+id/nav_chat\""));
        assertTrue(chatGraph.contains("app:startDestination=\"@id/chatFragment\""));
        assertTrue(toolbarMenu.contains("android:id=\"@+id/action_chat_history\""));
        assertTrue(toolbarMenu.contains("android:id=\"@+id/action_chat_new\""));
        assertTrue(strings.contains("<string name=\"chat_title\">Chat bot trợ lý NGBT</string>"));
        assertTrue(strings.contains("<string name=\"chat_composer_hint\">Hỏi về Món hoặc Order...</string>"));
        assertTrue(colors.contains("<color name=\"chat_orange\">#F5A623</color>"));
        assertFalse(fragmentLayout.contains("android:text=\"#"));
        assertTrue(activityLayout.contains("app:layout_constraintBottom_toTopOf=\"@id/bottomNav\""));
        assertFalse(activityLayout.contains("android:paddingBottom=\"56dp\""));
    }

    private static String readResource(String relativePath) throws IOException {
        Path path = Paths.get("src/main/res").resolve(relativePath);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
