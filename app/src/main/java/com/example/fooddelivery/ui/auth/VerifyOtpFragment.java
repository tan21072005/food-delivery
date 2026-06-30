package com.example.fooddelivery.ui.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;

public class VerifyOtpFragment extends Fragment {
    private CountDownTimer timer;
    private boolean requestLoading;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        return inflater.inflate(R.layout.auth_fragment_verify_otp, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        PasswordRecoveryViewModel vm =
                new ViewModelProvider(requireActivity()).get(PasswordRecoveryViewModel.class);
        if (vm.getEmail() == null) {
            Navigation.findNavController(view).popBackStack();
            return;
        }
        EditText otpInput = view.findViewById(R.id.edOtp);
        Button submit = view.findViewById(R.id.btnSubmitOtp);
        TextView resend = view.findViewById(R.id.tvResendCode);
        TextView description = view.findViewById(R.id.tvEmailDesc);
        description.setText(getString(R.string.desc_otp_sent, vm.getMaskedEmail()));

        vm.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean enabled = !Boolean.TRUE.equals(loading);
            requestLoading = !enabled;
            otpInput.setEnabled(enabled);
            submit.setEnabled(enabled);
            resend.setEnabled(enabled && vm.getResendSecondsRemaining() == 0);
        });
        vm.getEvents().observe(getViewLifecycleOwner(), source -> {
            RecoveryEvent event = source == null ? null : source.consume();
            if (event == null) return;
            if (event.type() == RecoveryEvent.Type.OTP_VERIFIED) {
                Navigation.findNavController(view).navigate(R.id.action_otp_to_reset);
            } else if (event.type() == RecoveryEvent.Type.CODE_RESENT) {
                Toast.makeText(requireContext(), R.string.msg_otp_resent, Toast.LENGTH_SHORT).show();
                startCooldown(resend, vm);
            } else if (event.type() == RecoveryEvent.Type.ERROR) {
                Toast.makeText(requireContext(), event.message(), Toast.LENGTH_SHORT).show();
                if (vm.getResendSecondsRemaining() > 0) {
                    startCooldown(resend, vm);
                }
            }
        });
        submit.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (!PasswordRecoveryValidator.isValidOtp(otp)) {
                otpInput.setError(getString(R.string.error_empty_otp));
                return;
            }
            vm.verifyOtp(otp);
        });
        resend.setOnClickListener(v -> {
            if (vm.getResendSecondsRemaining() == 0) vm.resendCode();
        });
        view.findViewById(R.id.tvChangeEmail).setOnClickListener(v -> {
            vm.restart();
            Navigation.findNavController(view).popBackStack();
        });
        view.findViewById(R.id.tvCantAccessEmail).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.msg_contact_support, Toast.LENGTH_SHORT).show());
        startCooldown(resend, vm);
    }

    private void startCooldown(TextView resend, PasswordRecoveryViewModel vm) {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(Math.max(1, vm.getResendSecondsRemaining()) * 1000, 1000) {
            @Override public void onTick(long ignored) {
                long remaining = vm.getResendSecondsRemaining();
                resend.setEnabled(!requestLoading && remaining == 0);
                resend.setText(remaining == 0 ? getString(R.string.btn_resend_otp)
                        : getString(R.string.btn_resend_otp_countdown, remaining));
            }
            @Override public void onFinish() {
                resend.setEnabled(!requestLoading);
                resend.setText(R.string.btn_resend_otp);
            }
        }.start();
    }

    @Override public void onDestroyView() {
        if (timer != null) timer.cancel();
        super.onDestroyView();
    }
}
