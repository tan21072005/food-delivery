package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.local.DeliveryAddressStore;
import com.example.fooddelivery.data.local.SharedPreferencesDeliveryAddressStore;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddressRepository {
    private static final String ADDRESS_SELECT =
            "id,customer_id,label,receiver_name,receiver_phone,address_line,building_name,floor,gate_note,latitude,longitude,is_default,created_at,updated_at";

    private final DeliveryAddressStore selectedStore;
    private final SessionManager sessionManager;
    private final ApiService apiService;

    public DeliveryAddressRepository(Context context) {
        this.selectedStore = new SharedPreferencesDeliveryAddressStore(context);
        this.sessionManager = new SessionManager(context);
        this.apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public DeliveryAddressRepository(DeliveryAddressStore store) {
        throw new IllegalStateException("Use DeliveryAddressRepository(Context) for Supabase delivery addresses");
    }

    public void list(ResultCallback<List<DeliveryAddress>> callback) {
        resolveCustomerId(new ResultCallback<Long>() {
            @Override
            public void onSuccess(Long customerId) {
                apiService.getDeliveryAddresses(
                        ADDRESS_SELECT,
                        "eq." + customerId,
                        "is.null",
                        "is_default.desc,created_at.desc"
                ).enqueue(new Callback<List<DeliveryAddress>>() {
                    @Override
                    public void onResponse(Call<List<DeliveryAddress>> call, Response<List<DeliveryAddress>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Khong tai duoc dia chi: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DeliveryAddress>> call, Throwable t) {
                        callback.onError("Loi ket noi dia chi: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void find(String id, ResultCallback<DeliveryAddress> callback) {
        if (id == null || id.trim().isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        apiService.getDeliveryAddressById("eq." + id, ADDRESS_SELECT)
                .enqueue(new Callback<List<DeliveryAddress>>() {
                    @Override
                    public void onResponse(Call<List<DeliveryAddress>> call, Response<List<DeliveryAddress>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<DeliveryAddress> addresses = response.body();
                            callback.onSuccess(addresses.isEmpty() ? null : addresses.get(0));
                        } else {
                            callback.onError("Khong tai duoc dia chi: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DeliveryAddress>> call, Throwable t) {
                        callback.onError("Loi ket noi dia chi: " + t.getMessage());
                    }
                });
    }

    public void getCurrentAddress(ResultCallback<DeliveryAddress> callback) {
        list(new ResultCallback<List<DeliveryAddress>>() {
            @Override
            public void onSuccess(List<DeliveryAddress> addresses) {
                String selectedId = selectedStore.getSelectedId();
                DeliveryAddress fallbackDefault = null;
                for (DeliveryAddress address : addresses) {
                    if (selectedId != null && selectedId.equals(address.getId())) {
                        callback.onSuccess(address);
                        return;
                    }
                    if (address.isDefault()) fallbackDefault = address;
                }
                callback.onSuccess(fallbackDefault != null
                        ? fallbackDefault
                        : (addresses.isEmpty() ? null : addresses.get(0)));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void save(DeliveryAddress draft, SaveCallback callback) {
        Map<String, String> errors = validate(draft);
        if (!errors.isEmpty()) {
            callback.onComplete(SaveResult.failure(errors));
            return;
        }

        resolveCustomerId(new ResultCallback<Long>() {
            @Override
            public void onSuccess(Long customerId) {
                Map<String, Object> body = toPayload(draft, customerId);
                boolean isNew = draft.getId() == null || draft.getId().trim().isEmpty();
                if (isNew) {
                    createRemote(body, callback);
                } else {
                    updateRemote(draft.getId(), body, callback);
                }
            }

            @Override
            public void onError(String message) {
                callback.onComplete(SaveResult.failure(persistenceError(message)));
            }
        });
    }

    public void select(String id) {
        selectedStore.setSelectedId(id);
    }

    public void setDefault(String id, ResultCallback<Void> callback) {
        resolveCustomerId(new ResultCallback<Long>() {
            @Override
            public void onSuccess(Long customerId) {
                Map<String, Object> clearBody = new LinkedHashMap<>();
                clearBody.put("is_default", false);
                apiService.updateDeliveryAddressDefaults("eq." + customerId, "eq.true", clearBody)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Map<String, Object> setBody = new LinkedHashMap<>();
                                    setBody.put("is_default", true);
                                    apiService.updateDeliveryAddress("eq." + id, setBody)
                                            .enqueue(new Callback<List<DeliveryAddress>>() {
                                                @Override
                                                public void onResponse(Call<List<DeliveryAddress>> call, Response<List<DeliveryAddress>> response) {
                                                    if (response.isSuccessful()) {
                                                        selectedStore.setSelectedId(id);
                                                        callback.onSuccess(null);
                                                    } else {
                                                        callback.onError("Khong dat duoc mac dinh: " + response.code());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<List<DeliveryAddress>> call, Throwable t) {
                                                    callback.onError("Loi ket noi: " + t.getMessage());
                                                }
                                            });
                                } else {
                                    callback.onError("Khong cap nhat mac dinh: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                callback.onError("Loi ket noi: " + t.getMessage());
                            }
                        });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void delete(String id, ResultCallback<Void> callback) {
        apiService.deleteDeliveryAddress("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (id != null && id.equals(selectedStore.getSelectedId())) selectedStore.setSelectedId(null);
                    callback.onSuccess(null);
                } else {
                    callback.onError("Khong xoa duoc dia chi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Loi ket noi: " + t.getMessage());
            }
        });
    }

    private void createRemote(Map<String, Object> body, SaveCallback callback) {
        body.put("is_default", true);
        list(new ResultCallback<List<DeliveryAddress>>() {
            @Override
            public void onSuccess(List<DeliveryAddress> addresses) {
                if (!addresses.isEmpty()) body.put("is_default", false);
                apiService.createDeliveryAddress(body).enqueue(saveResponseCallback(callback));
            }

            @Override
            public void onError(String message) {
                callback.onComplete(SaveResult.failure(persistenceError(message)));
            }
        });
    }

    private void updateRemote(String id, Map<String, Object> body, SaveCallback callback) {
        apiService.updateDeliveryAddress("eq." + id, body).enqueue(saveResponseCallback(callback));
    }

    private Callback<List<DeliveryAddress>> saveResponseCallback(SaveCallback callback) {
        return new Callback<List<DeliveryAddress>>() {
            @Override
            public void onResponse(Call<List<DeliveryAddress>> call, Response<List<DeliveryAddress>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    DeliveryAddress saved = response.body().get(0);
                    selectedStore.setSelectedId(saved.getId());
                    callback.onComplete(SaveResult.success(saved));
                } else {
                    callback.onComplete(SaveResult.failure(persistenceError("Supabase response " + response.code())));
                }
            }

            @Override
            public void onFailure(Call<List<DeliveryAddress>> call, Throwable t) {
                callback.onComplete(SaveResult.failure(persistenceError(t.getMessage())));
            }
        };
    }

    private void resolveCustomerId(ResultCallback<Long> callback) {
        int savedId = sessionManager.getUserId();
        if (savedId > 0) {
            callback.onSuccess((long) savedId);
            return;
        }

        String email = sessionManager.getEmail();
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Ban can dang nhap de luu dia chi");
            return;
        }

        apiService.getUserByEmail("eq." + email.trim()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long userId = response.body().get(0).getId();
                    if (userId > 0 && userId <= Integer.MAX_VALUE) {
                        sessionManager.saveSession(
                                sessionManager.getToken(),
                                (int) userId,
                                sessionManager.getUserName(),
                                sessionManager.getEmail(),
                                sessionManager.getRole()
                        );
                    }
                    callback.onSuccess(userId);
                } else {
                    callback.onError("Khong tim thay ho so nguoi dung tren Supabase");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError("Loi ket noi nguoi dung: " + t.getMessage());
            }
        });
    }

    private Map<String, Object> toPayload(DeliveryAddress draft, long customerId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customer_id", customerId);
        body.put("label", displayLabel(draft));
        body.put("receiver_name", trim(draft.getRecipientName()));
        body.put("receiver_phone", trim(draft.getRecipientPhone()));
        body.put("address_line", trim(draft.getFullAddress()));
        body.put("floor", trim(draft.getBuildingFloor()));
        body.put("gate_note", trim(draft.getGate()));
        body.put("latitude", draft.getLatitude());
        body.put("longitude", draft.getLongitude());
        body.put("is_default", draft.isDefault());
        return body;
    }

    private String displayLabel(DeliveryAddress draft) {
        if ("Khac".equals(draft.getType()) && !isBlank(draft.getCustomName())) {
            return trim(draft.getCustomName());
        }
        return isBlank(draft.getType()) ? "Nha" : trim(draft.getType());
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

    private Map<String, String> persistenceError(String message) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("persistence", message == null ? "Could not save delivery address" : message);
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public interface ResultCallback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    public interface SaveCallback {
        void onComplete(SaveResult result);
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
