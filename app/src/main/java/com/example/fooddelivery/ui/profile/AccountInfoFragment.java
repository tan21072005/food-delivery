package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fooddelivery.R;

public class AccountInfoFragment extends Fragment {

    private AccountInfoViewModel viewModel;
    private TextView tvNameValue;
    private TextView tvPhoneValue;
    private TextView tvEmailValue;
    private TextView tvBirthdayValue;
    private TextView tvCountryValue;

    public AccountInfoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountInfoViewModel.class);
        tvNameValue = view.findViewById(R.id.tvNameValue);
        tvPhoneValue = view.findViewById(R.id.tvPhoneValue);
        tvEmailValue = view.findViewById(R.id.tvEmailValue);
        tvBirthdayValue = view.findViewById(R.id.tvBirthdayValue);
        tvCountryValue = view.findViewById(R.id.tvCountryValue);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadAccountInfo();
    }

    private void render(AccountInfoViewModel.AccountInfoUiState state) {
        tvNameValue.setText(state.name);
        tvPhoneValue.setText(state.phoneNumber);
        tvEmailValue.setText(state.email);
        tvBirthdayValue.setText(state.birthDate);
        tvCountryValue.setText(state.country);
    }
}
