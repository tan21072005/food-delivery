package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.SharedPreferencesDeliveryAddressStore;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

public class DeliveryAddressFormFragment extends Fragment {

    private DeliveryAddressRepository repository;
    private EditText etRecipientName;
    private EditText etRecipientPhone;
    private EditText etFullAddress;
    private EditText etBuildingFloor;
    private EditText etGate;
    private EditText etCustomName;
    private EditText etDriverNote;
    private RadioGroup rgAddressType;
    private TextView tvDeliveryAddressFormTitle;
    private MaterialButton btnSaveAddress;
    private View groupCustomName;
    private String source = "profile";
    private String addressId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_address_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new DeliveryAddressRepository(new SharedPreferencesDeliveryAddressStore(requireContext()));

        if (getArguments() != null) {
            source = getArguments().getString("source", "profile");
            addressId = getArguments().getString("addressId");
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        etRecipientName = view.findViewById(R.id.etRecipientName);
        etRecipientPhone = view.findViewById(R.id.etRecipientPhone);
        etFullAddress = view.findViewById(R.id.etFullAddress);
        etBuildingFloor = view.findViewById(R.id.etBuildingFloor);
        etGate = view.findViewById(R.id.etGate);
        etCustomName = view.findViewById(R.id.etCustomName);
        etDriverNote = view.findViewById(R.id.etDriverNote);
        rgAddressType = view.findViewById(R.id.rgAddressType);
        groupCustomName = etCustomName;
        tvDeliveryAddressFormTitle = view.findViewById(R.id.tvDeliveryAddressFormTitle);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);

        DeliveryAddress existing = repository.find(addressId);
        boolean editMode = existing != null;
        tvDeliveryAddressFormTitle.setText(editMode ? "Sua dia chi" : "Them dia chi moi");
        btnSaveAddress.setText(editMode ? "Cap nhat dia chi" : "Luu dia chi");
        if (existing != null) {
            bind(existing);
        } else if (getArguments() != null && getArguments().getString("prefillType") != null) {
            setType(getArguments().getString("prefillType"));
        } else {
            setType("Nha");
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveEnabled();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etRecipientName.addTextChangedListener(watcher);
        etRecipientPhone.addTextChangedListener(watcher);
        etFullAddress.addTextChangedListener(watcher);
        etCustomName.addTextChangedListener(watcher);
        rgAddressType.setOnCheckedChangeListener((group, checkedId) -> {
            groupCustomName.setVisibility("Khac".equals(getSelectedType()) ? View.VISIBLE : View.GONE);
            updateSaveEnabled();
        });

        btnSaveAddress.setOnClickListener(v -> save());
        updateSaveEnabled();
    }

    private void bind(DeliveryAddress address) {
        etRecipientName.setText(address.getRecipientName());
        etRecipientPhone.setText(address.getRecipientPhone());
        etFullAddress.setText(address.getFullAddress());
        etBuildingFloor.setText(address.getBuildingFloor());
        etGate.setText(address.getGate());
        etCustomName.setText(address.getCustomName());
        etDriverNote.setText(address.getDriverNote());
        setType(address.getType());
    }

    private void save() {
        DeliveryAddress draft = new DeliveryAddress();
        draft.setId(addressId);
        draft.setType(getSelectedType());
        draft.setRecipientName(text(etRecipientName));
        draft.setRecipientPhone(text(etRecipientPhone));
        draft.setFullAddress(text(etFullAddress));
        draft.setBuildingFloor(text(etBuildingFloor));
        draft.setGate(text(etGate));
        draft.setCustomName(text(etCustomName));
        draft.setDriverNote(text(etDriverNote));

        DeliveryAddress existing = repository.find(addressId);
        if (existing != null) draft.setDefault(existing.isDefault());

        DeliveryAddressRepository.SaveResult result = repository.save(draft);
        if (!result.isSuccess()) {
            showErrors(result.getErrors());
            return;
        }

        if ("home".equals(source)) {
            Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
        } else {
            Navigation.findNavController(requireView()).popBackStack(R.id.addressListFragment, false);
        }
    }

    private void showErrors(Map<String, String> errors) {
        etRecipientName.setError(errors.get("recipientName"));
        etRecipientPhone.setError(errors.get("recipientPhone"));
        etFullAddress.setError(errors.get("fullAddress"));
        etCustomName.setError(errors.get("customName"));
        String message = errors.containsKey("persistence")
                ? "Khong the luu dia chi. Thu lai sau"
                : "Kiem tra lai thong tin dia chi";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateSaveEnabled() {
        boolean valid = !text(etRecipientName).isEmpty()
                && !text(etRecipientPhone).isEmpty()
                && !text(etFullAddress).isEmpty()
                && (!"Khac".equals(getSelectedType()) || !text(etCustomName).isEmpty());
        btnSaveAddress.setEnabled(valid);
        btnSaveAddress.setAlpha(valid ? 1f : 0.45f);
    }

    private String getSelectedType() {
        int checkedId = rgAddressType.getCheckedRadioButtonId();
        RadioButton button = checkedId == View.NO_ID ? null : rgAddressType.findViewById(checkedId);
        return button == null ? "" : button.getText().toString();
    }

    private void setType(String type) {
        if ("Cong ty".equals(type)) {
            rgAddressType.check(R.id.rbWork);
        } else if ("Khac".equals(type)) {
            rgAddressType.check(R.id.rbOther);
        } else {
            rgAddressType.check(R.id.rbHome);
        }
        groupCustomName.setVisibility("Khac".equals(getSelectedType()) ? View.VISIBLE : View.GONE);
    }

    private String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
