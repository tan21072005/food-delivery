package com.example.fooddelivery.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;

public class ForgetPassFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        return inflater.inflate(R.layout.auth_fragment_forget_password, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        PasswordRecoveryViewModel vm =
                new ViewModelProvider(requireActivity()).get(PasswordRecoveryViewModel.class);
        EditText emailInput = view.findViewById(R.id.edEmailPhone);
        Button next = view.findViewById(R.id.btnNext);

        vm.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean enabled = !Boolean.TRUE.equals(loading);
            emailInput.setEnabled(enabled);
            next.setEnabled(enabled);
        });
        vm.getEvents().observe(getViewLifecycleOwner(), source -> {
            RecoveryEvent event = source == null ? null : source.consume();
            if (event == null) return;
            if (event.type() == RecoveryEvent.Type.CODE_SENT) {
                Navigation.findNavController(view).navigate(R.id.action_forget_to_otp);
            } else if (event.type() == RecoveryEvent.Type.ERROR) {
                Toast.makeText(requireContext(), event.message(), Toast.LENGTH_SHORT).show();
            }
        });
        next.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!PasswordRecoveryValidator.isValidEmail(email)) {
                emailInput.setError(getString(R.string.error_invalid_email));
                return;
            }
            String suggestion = PasswordRecoveryValidator.suggestEmailCorrection(email);
            if (suggestion != null) {
                emailInput.setError(getString(R.string.error_email_domain_typo, suggestion));
                return;
            }
            vm.requestCode(email);
        });
        view.findViewById(R.id.tvBack).setOnClickListener(v -> {
            vm.restart();
            Navigation.findNavController(view).popBackStack();
        });
    }
}
