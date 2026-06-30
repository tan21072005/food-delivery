package com.example.fooddelivery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationRepositoryTest {

    @Test
    public void currentLocationDoesNotFallbackToDemoCoordinates() throws Exception {
        String source = readFile(projectPath(
                "src/main/java/com/example/fooddelivery/data/repository/LocationRepository.java"));

        assertFalse(source.contains("Location(\"dummy\")"));
        assertFalse(source.contains("21.0028"));
        assertFalse(source.contains("105.8427"));
        assertFalse(source.contains("mockLocation"));
    }

    @Test
    public void currentLocationReportsEmptyStateWhenUnavailable() throws Exception {
        String source = readFile(projectPath(
                "src/main/java/com/example/fooddelivery/data/repository/LocationRepository.java"));

        assertTrue(source.contains("locationData.setValue(null);"));
        assertTrue(source.contains("addOnFailureListener"));
    }

    private Path projectPath(String path) {
        Path moduleRelative = Paths.get(path);
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("app").resolve(path);
    }

    private String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
