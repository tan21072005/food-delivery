package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.local.DeliveryAddressStore;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeliveryAddressRepositoryTest {

    @Test
    public void storeConstructorFailsFastBecauseDeliveryAddressesUseSupabase() {
        try {
            new DeliveryAddressRepository(new MemoryStore());
        } catch (IllegalStateException exception) {
            assertEquals("Use DeliveryAddressRepository(Context) for Supabase delivery addresses",
                    exception.getMessage());
            return;
        }

        throw new AssertionError("Expected DeliveryAddressRepository(DeliveryAddressStore) to fail");
    }

    @Test
    public void repositoryExposesCallbackApiForRemoteDeliveryAddresses() throws Exception {
        String source = readFile(projectPath(
                "src/main/java/com/example/fooddelivery/data/repository/DeliveryAddressRepository.java"));

        assertTrue(source.contains("public void list(ResultCallback<List<DeliveryAddress>> callback)"));
        assertTrue(source.contains("public void getCurrentAddress(ResultCallback<DeliveryAddress> callback)"));
        assertTrue(source.contains("public void save(DeliveryAddress draft, SaveCallback callback)"));
        assertTrue(source.contains("public void setDefault(String id, ResultCallback<Void> callback)"));
        assertTrue(source.contains("public void delete(String id, ResultCallback<Void> callback)"));

        assertFalse(source.contains("public List<DeliveryAddress> list()"));
        assertFalse(source.contains("public DeliveryAddress getCurrentAddress()"));
        assertFalse(source.contains("public SaveResult save(DeliveryAddress draft)"));
        assertFalse(source.contains("public boolean select(String id)"));
    }

    @Test
    public void saveResultFailureCarriesValidationErrorsWithoutAddress() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("recipientName", "Recipient name is required");

        DeliveryAddressRepository.SaveResult result =
                DeliveryAddressRepository.SaveResult.failure(errors);

        assertFalse(result.isSuccess());
        assertNull(result.getAddress());
        assertEquals("Recipient name is required", result.getErrors().get("recipientName"));
    }

    private Path projectPath(String path) {
        Path moduleRelative = Paths.get(path);
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("app").resolve(path);
    }

    private String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
    }

    private static class MemoryStore implements DeliveryAddressStore {
        @Override
        public List<com.example.fooddelivery.data.model.DeliveryAddress> load() {
            return java.util.Collections.emptyList();
        }

        @Override
        public void save(List<com.example.fooddelivery.data.model.DeliveryAddress> addresses) {
        }

        @Override
        public String newId() {
            return "1";
        }

        @Override
        public String getSelectedId() {
            return null;
        }

        @Override
        public void setSelectedId(String id) {
        }
    }
}
