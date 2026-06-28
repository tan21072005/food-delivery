package com.example.fooddelivery.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddressViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<DeliveryAddress>> addresses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<DeliveryAddress> selectedAddress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final DeliveryAddressRepository repository;

    public DeliveryAddressViewModel(@NonNull Application application) {
        super(application);
        repository = new DeliveryAddressRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<DeliveryAddress>> getAddresses() {
        return addresses;
    }

    public LiveData<DeliveryAddress> getSelectedAddress() {
        return selectedAddress;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadAddresses() {
        isLoading.setValue(true);
        repository.list().enqueue(new Callback<List<DeliveryAddress>>() {
            @Override
            public void onResponse(Call<List<DeliveryAddress>> call, Response<List<DeliveryAddress>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    addresses.setValue(response.body());
                    selectedAddress.setValue(findDefault(response.body()));
                } else {
                    error.setValue("Không tải được DeliveryAddress: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<DeliveryAddress>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Lỗi kết nối DeliveryAddress: " + t.getMessage());
            }
        });
    }

    public void setDefault(DeliveryAddress address) {
        if (address == null || address.getId() == null) {
            return;
        }
        isLoading.setValue(true);
        repository.setDefault(address.getId()).enqueue(reloadAfterMutation("Không đặt được mặc định"));
    }

    public void softDelete(DeliveryAddress address) {
        if (address == null || address.getId() == null) {
            return;
        }
        isLoading.setValue(true);
        repository.softDelete(address.getId()).enqueue(reloadAfterMutation("Không xóa được DeliveryAddress"));
    }

    private Callback<Void> reloadAfterMutation(String failurePrefix) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadAddresses();
                } else {
                    isLoading.setValue(false);
                    error.setValue(failurePrefix + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(failurePrefix + ": " + t.getMessage());
            }
        };
    }

    private DeliveryAddress findDefault(List<DeliveryAddress> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (DeliveryAddress address : list) {
            if (address.isDefault()) {
                return address;
            }
        }
        return list.get(0);
    }
}
