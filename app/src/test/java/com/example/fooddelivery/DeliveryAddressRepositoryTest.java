package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.local.DeliveryAddressStore;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAddressRepositoryTest {

    @Test
    public void saveWithoutRequiredFieldsReturnsValidationErrorsAndKeepsStoreEmpty() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress draft = new DeliveryAddress();
        draft.setType("Nha");

        DeliveryAddressRepository.SaveResult result = repository.save(draft);

        assertFalse(result.isSuccess());
        assertEquals("Recipient name is required", result.getErrors().get("recipientName"));
        assertEquals("Recipient phone is required", result.getErrors().get("recipientPhone"));
        assertEquals("Full address is required", result.getErrors().get("fullAddress"));
        assertEquals(0, repository.list().size());
    }

    @Test
    public void khacTypeRequiresCustomName() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress draft = validDraft("Nha");
        draft.setType("Khac");
        draft.setCustomName("");

        DeliveryAddressRepository.SaveResult result = repository.save(draft);

        assertFalse(result.isSuccess());
        assertEquals("Address name is required", result.getErrors().get("customName"));
    }

    @Test
    public void firstSavedAddressBecomesDefaultAndCurrent() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());

        DeliveryAddressRepository.SaveResult result = repository.save(validDraft("Nha"));

        assertTrue(result.isSuccess());
        assertTrue(result.getAddress().isDefault());
        assertEquals(result.getAddress().getId(), repository.getCurrentAddress().getId());
    }

    @Test
    public void selectingExistingAddressUpdatesCurrentAddress() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress first = repository.save(validDraft("Nha")).getAddress();
        DeliveryAddress second = repository.save(validDraft("Cong ty")).getAddress();

        assertTrue(repository.select(second.getId()));

        assertEquals(second.getId(), repository.getCurrentAddress().getId());
        assertFalse(first.getId().equals(repository.getCurrentAddress().getId()));
    }

    @Test
    public void deletingCurrentDefaultPromotesRemainingAddressAndClearsStaleSelection() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress first = repository.save(validDraft("Nha")).getAddress();
        DeliveryAddress second = repository.save(validDraft("Cong ty")).getAddress();
        repository.select(first.getId());

        repository.delete(first.getId());

        assertEquals(1, repository.list().size());
        assertEquals(second.getId(), repository.getCurrentAddress().getId());
        assertTrue(repository.getCurrentAddress().isDefault());
    }

    @Test
    public void editingCurrentAddressKeepsItCurrentAndUpdatesVisibleFields() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress first = repository.save(validDraft("Nha")).getAddress();
        repository.save(validDraft("Cong ty"));
        repository.select(first.getId());

        DeliveryAddress edit = validDraft("Khac");
        edit.setId(first.getId());
        edit.setCustomName("Nha rieng");
        edit.setFullAddress("88 Nguyen Trai, Thanh Xuan, Ha Noi");

        DeliveryAddressRepository.SaveResult result = repository.save(edit);

        assertTrue(result.isSuccess());
        assertEquals(first.getId(), repository.getCurrentAddress().getId());
        assertEquals("Nha rieng", repository.getCurrentAddress().getDisplayLabel());
        assertEquals("88 Nguyen Trai, Thanh Xuan, Ha Noi", repository.getCurrentAddress().getFullAddress());
        assertTrue(repository.getCurrentAddress().isDefault());
    }

    @Test
    public void listPlacesDefaultAddressFirstAfterDefaultChanges() {
        DeliveryAddressRepository repository = new DeliveryAddressRepository(new MemoryStore());
        DeliveryAddress first = repository.save(validDraft("Nha")).getAddress();
        DeliveryAddress second = repository.save(validDraft("Cong ty")).getAddress();

        repository.setDefault(second.getId());

        List<DeliveryAddress> addresses = repository.list();
        assertEquals(second.getId(), addresses.get(0).getId());
        assertTrue(addresses.get(0).isDefault());
        assertEquals(first.getId(), addresses.get(1).getId());
    }

    private DeliveryAddress validDraft(String type) {
        DeliveryAddress draft = new DeliveryAddress();
        draft.setType(type);
        draft.setRecipientName("Nguyen Van A");
        draft.setRecipientPhone("0901234567");
        draft.setFullAddress("72 Tran Dai Nghia, Hai Ba Trung, Ha Noi");
        if ("Khac".equals(type)) {
            draft.setCustomName("Phong tap");
        }
        return draft;
    }

    private static class MemoryStore implements DeliveryAddressStore {
        private final List<DeliveryAddress> addresses = new ArrayList<>();
        private String selectedId;
        private int nextId = 1;

        @Override
        public List<DeliveryAddress> load() {
            return new ArrayList<>(addresses);
        }

        @Override
        public void save(List<DeliveryAddress> newAddresses) {
            addresses.clear();
            addresses.addAll(newAddresses);
        }

        @Override
        public String newId() {
            return String.valueOf(nextId++);
        }

        @Override
        public String getSelectedId() {
            return selectedId;
        }

        @Override
        public void setSelectedId(String id) {
            selectedId = id;
        }
    }
}
