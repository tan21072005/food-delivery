package com.example.fooddelivery.data.repository;

import com.example.fooddelivery.data.local.DeliveryAddressStore;
import com.example.fooddelivery.data.model.DeliveryAddress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeliveryAddressRepository {
    private final DeliveryAddressStore store;

    public DeliveryAddressRepository(DeliveryAddressStore store) {
        this.store = store;
    }

    public List<DeliveryAddress> list() {
        List<DeliveryAddress> addresses = store.load();
        sortDefaultFirst(addresses);
        return addresses;
    }

    public DeliveryAddress find(String id) {
        if (id == null) return null;
        for (DeliveryAddress address : store.load()) {
            if (id.equals(address.getId())) return address;
        }
        return null;
    }

    public DeliveryAddress getCurrentAddress() {
        List<DeliveryAddress> addresses = store.load();
        String selectedId = store.getSelectedId();
        DeliveryAddress fallbackDefault = null;
        for (DeliveryAddress address : addresses) {
            if (selectedId != null && selectedId.equals(address.getId())) return address;
            if (address.isDefault()) fallbackDefault = address;
        }
        return fallbackDefault != null ? fallbackDefault : (addresses.isEmpty() ? null : addresses.get(0));
    }

    public SaveResult save(DeliveryAddress draft) {
        Map<String, String> errors = validate(draft);
        if (!errors.isEmpty()) {
            return SaveResult.failure(errors);
        }

        List<DeliveryAddress> addresses = store.load();
        DeliveryAddress saved = copy(draft);
        boolean isNew = saved.getId() == null || saved.getId().trim().isEmpty();
        if (isNew) {
            saved.setId(store.newId());
        }

        if (addresses.isEmpty()) {
            saved.setDefault(true);
        }
        if (saved.isDefault()) {
            for (DeliveryAddress address : addresses) {
                address.setDefault(false);
            }
        }

        boolean replaced = false;
        for (int i = 0; i < addresses.size(); i++) {
            if (saved.getId().equals(addresses.get(i).getId())) {
                addresses.set(i, saved);
                replaced = true;
                break;
            }
        }
        if (!replaced) addresses.add(saved);

        ensureOneDefault(addresses);
        store.save(addresses);
        store.setSelectedId(saved.getId());
        return SaveResult.success(saved);
    }

    public boolean select(String id) {
        DeliveryAddress address = find(id);
        if (address == null) return false;
        store.setSelectedId(id);
        return true;
    }

    public void setDefault(String id) {
        List<DeliveryAddress> addresses = store.load();
        boolean found = false;
        for (DeliveryAddress address : addresses) {
            boolean selected = id != null && id.equals(address.getId());
            address.setDefault(selected);
            found = found || selected;
        }
        if (found) {
            store.save(addresses);
            store.setSelectedId(id);
        }
    }

    public void delete(String id) {
        List<DeliveryAddress> addresses = new ArrayList<>();
        boolean deletedDefault = false;
        for (DeliveryAddress address : store.load()) {
            if (id != null && id.equals(address.getId())) {
                deletedDefault = address.isDefault();
            } else {
                addresses.add(address);
            }
        }
        if (deletedDefault && !addresses.isEmpty()) {
            addresses.get(0).setDefault(true);
        }
        ensureOneDefault(addresses);
        store.save(addresses);
        if (id != null && id.equals(store.getSelectedId())) {
            DeliveryAddress current = getCurrentFrom(addresses);
            store.setSelectedId(current == null ? null : current.getId());
        }
    }

    private Map<String, String> validate(DeliveryAddress draft) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (isBlank(draft.getRecipientName())) errors.put("recipientName", "Recipient name is required");
        if (isBlank(draft.getRecipientPhone())) errors.put("recipientPhone", "Recipient phone is required");
        if (isBlank(draft.getFullAddress())) errors.put("fullAddress", "Full address is required");
        if (isBlank(draft.getType())) errors.put("type", "Address type is required");
        if ("Khac".equals(draft.getType()) && isBlank(draft.getCustomName())) {
            errors.put("customName", "Address name is required");
        }
        return errors;
    }

    private void ensureOneDefault(List<DeliveryAddress> addresses) {
        if (addresses.isEmpty()) return;
        DeliveryAddress firstDefault = null;
        for (DeliveryAddress address : addresses) {
            if (address.isDefault()) {
                if (firstDefault == null) {
                    firstDefault = address;
                } else {
                    address.setDefault(false);
                }
            }
        }
        if (firstDefault == null) addresses.get(0).setDefault(true);
    }

    private void sortDefaultFirst(List<DeliveryAddress> addresses) {
        addresses.sort((first, second) -> Boolean.compare(second.isDefault(), first.isDefault()));
    }

    private DeliveryAddress getCurrentFrom(List<DeliveryAddress> addresses) {
        for (DeliveryAddress address : addresses) {
            if (address.isDefault()) return address;
        }
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DeliveryAddress copy(DeliveryAddress source) {
        DeliveryAddress copy = new DeliveryAddress();
        copy.setId(source.getId());
        copy.setType(trim(source.getType()));
        copy.setRecipientName(trim(source.getRecipientName()));
        copy.setRecipientPhone(trim(source.getRecipientPhone()));
        copy.setFullAddress(trim(source.getFullAddress()));
        copy.setBuildingFloor(trim(source.getBuildingFloor()));
        copy.setGate(trim(source.getGate()));
        copy.setCustomName(trim(source.getCustomName()));
        copy.setDriverNote(trim(source.getDriverNote()));
        copy.setLatitude(source.getLatitude());
        copy.setLongitude(source.getLongitude());
        copy.setDefault(source.isDefault());
        return copy;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static class SaveResult {
        private final boolean success;
        private final DeliveryAddress address;
        private final Map<String, String> errors;

        private SaveResult(boolean success, DeliveryAddress address, Map<String, String> errors) {
            this.success = success;
            this.address = address;
            this.errors = errors;
        }

        public static SaveResult success(DeliveryAddress address) {
            return new SaveResult(true, address, new LinkedHashMap<>());
        }

        public static SaveResult failure(Map<String, String> errors) {
            return new SaveResult(false, null, errors);
        }

        public boolean isSuccess() {
            return success;
        }

        public DeliveryAddress getAddress() {
            return address;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}
