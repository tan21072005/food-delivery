package com.example.fooddelivery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SupabaseSeedOptionsCoverageTest {
    private static final List<String> SEEDED_MENU_ITEMS = Arrays.asList(
            "Bun bo dac biet",
            "Bun bo tai",
            "Bun bo gio heo",
            "Cha cua them",
            "Tra dao cam sa",
            "Tra sua tran chau duong den",
            "Tra vai hoa hong",
            "Tra sua oolong nuong",
            "Pizza hai san size M",
            "Pizza pepperoni size M",
            "Pizza margherita size M",
            "Spaghetti bo bam",
            "Set sushi ca hoi 8 mieng",
            "Sashimi ca hoi",
            "Set sushi tong hop 12 mieng"
    );

    @Test
    public void seedAddsOptionGroupsForEverySeededMenuItem() throws Exception {
        String seed = readSeed();
        String optionGroupSection = seed.substring(seed.indexOf("seed_option_groups"));

        assertTrue(optionGroupSection.contains("seed_option_groups"));
        assertTrue(optionGroupSection.contains("join public.restaurants r on r.name = sog.restaurant_name"));
        assertTrue(optionGroupSection.contains("join public.menu_items mi on mi.restaurant_id = r.id and mi.name = sog.menu_name"));

        for (String menuItem : SEEDED_MENU_ITEMS) {
            assertTrue("Missing option group seed for: " + menuItem,
                    optionGroupSection.contains("'" + menuItem + "'"));
        }
    }

    @Test
    public void seedAddsVietnameseOptionChoicesForAllOptionGroups() throws Exception {
        String seed = readSeed();
        String optionChoiceSection = seed.substring(seed.indexOf("seed_option_choices"));

        assertTrue(optionChoiceSection.contains("seed_option_choices"));
        assertTrue(optionChoiceSection.contains("Cỡ thường"));
        assertTrue(optionChoiceSection.contains("Ít đường"));
        assertTrue(optionChoiceSection.contains("Thêm phô mai"));
        assertTrue(optionChoiceSection.contains("Nước tương"));
    }

    private String readSeed() throws Exception {
        return new String(Files.readAllBytes(projectPath("docs/supabase_v3_food_delivery_seed.sql")),
                StandardCharsets.UTF_8);
    }

    private Path projectPath(String path) {
        Path direct = Paths.get(path);
        if (Files.exists(direct)) {
            return direct;
        }
        return Paths.get("..").resolve(path).normalize();
    }
}
