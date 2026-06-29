package com.example.fooddelivery;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;

public class FavoriteCollectionLayoutRegressionTest {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    @Test public void collectionNameSuggestionsCanBeInflated() throws Exception {
        Path path = Paths.get("src/main/res/layout/fragment_collection_name.xml");
        if (!Files.exists(path)) path = Paths.get("app").resolve(path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(Files.newInputStream(path));
        NodeList chips = document.getElementsByTagName("com.google.android.material.chip.Chip");

        for (int index = 0; index < chips.getLength(); index++) {
            Element chip = (Element) chips.item(index);
            assertFalse("Chip " + index + " thiếu layout_width",
                    chip.getAttributeNS(ANDROID_NS, "layout_width").isEmpty());
            assertFalse("Chip " + index + " thiếu layout_height",
                    chip.getAttributeNS(ANDROID_NS, "layout_height").isEmpty());
        }
    }
}
