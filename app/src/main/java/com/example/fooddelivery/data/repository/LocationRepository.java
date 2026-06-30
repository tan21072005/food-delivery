package com.example.fooddelivery.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationRepository {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationRepository(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public LiveData<Location> getCurrentLocation() {
        MutableLiveData<Location> locationData = new MutableLiveData<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(locationData::setValue)
                    .addOnFailureListener(exception -> locationData.setValue(null));
        } else {
            locationData.setValue(null);
        }

        return locationData;
    }
}
