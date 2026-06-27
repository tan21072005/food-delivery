package com.example.fooddelivery.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;

public class PasswordFormFragment extends Fragment {
    public static final String ARG_MODE = "MODE";
    public static final String MODE_CREATE = "CREATE";
    public static final String MODE_CHANGE = "CHANGE";
    public static final String MODE_RESET = "RESET";
    private String currentMode = MODE_CHANGE;
    private EditText oldPassword, newPassword, confirmPassword;
    private Button submit;
    private PasswordRecoveryViewModel recoveryViewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_password_form, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        if (getArguments() != null) currentMode = getArguments().getString(ARG_MODE, MODE_CHANGE);
        TextView title = view.findViewById(R.id.tvPasswordTitle);
        View oldLayout = view.findViewById(R.id.layoutOldPassword);
        oldPassword = view.findViewById(R.id.edOldPassword);
        newPassword = view.findViewById(R.id.edNewPassword);
        confirmPassword = view.findViewById(R.id.edConfirmPassword);
        submit = view.findViewById(R.id.btnSubmitPassword);
        if (MODE_CREATE.equals(currentMode)) {
            title.setText(R.string.title_create_password); oldLayout.setVisibility(View.GONE);
        } else if (MODE_CHANGE.equals(currentMode)) {
            title.setText(R.string.title_change_password); oldLayout.setVisibility(View.VISIBLE);
        } else {
            title.setText(R.string.title_reset_password); oldLayout.setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.tvPasswordRules))
                    .setText(R.string.recovery_password_rules);
            recoveryViewModel = new ViewModelProvider(requireActivity())
                    .get(PasswordRecoveryViewModel.class);
            if (!recoveryViewModel.hasVerifiedRecovery()) {
                Navigation.findNavController(view).popBackStack(R.id.forgetPassFragment, false);
                return;
            }
            observeRecovery(view);
        }
        setupToggle(view.findViewById(R.id.ivToggleNew), newPassword);
        setupToggle(view.findViewById(R.id.ivToggleConfirm), confirmPassword);
        submit.setOnClickListener(v -> handleSubmit(view));
    }

    private void observeRecovery(View view) {
        recoveryViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean enabled = !Boolean.TRUE.equals(loading);
            newPassword.setEnabled(enabled); confirmPassword.setEnabled(enabled); submit.setEnabled(enabled);
        });
        recoveryViewModel.getEvents().observe(getViewLifecycleOwner(), source -> {
            RecoveryEvent event = source == null ? null : source.consume();
            if (event == null) return;
            if (event.type() == RecoveryEvent.Type.PASSWORD_UPDATED) {
                Toast.makeText(requireContext(), R.string.msg_change_password_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_reset_to_login);
            } else if (event.type() == RecoveryEvent.Type.ERROR) {
                Toast.makeText(requireContext(), event.message(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSubmit(View view) {
        String oldValue = oldPassword.getText().toString().trim();
        String newValue = newPassword.getText().toString();
        String confirmValue = confirmPassword.getText().toString();
        if (MODE_RESET.equals(currentMode)) {
            recoveryViewModel.updatePassword(newValue, confirmValue);
            return;
        }
        if (MODE_CHANGE.equals(currentMode) && TextUtils.isEmpty(oldValue)) {
            oldPassword.setError("Vui lòng nhập mật khẩu cũ"); return;
        }
        if (TextUtils.isEmpty(newValue) || newValue.length() < 6) {
            newPassword.setError("Mật khẩu phải từ 6 ký tự"); return;
        }
        if (!newValue.equals(confirmValue)) {
            confirmPassword.setError("Mật khẩu không khớp"); return;
        }
        Toast.makeText(getContext(), "Xử lý thành công!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).popBackStack();
    }

    private void setupToggle(ImageView icon, EditText input) {
        icon.setOnClickListener(v -> {
            boolean hidden = input.getTransformationMethod() instanceof PasswordTransformationMethod;
            input.setTransformationMethod(hidden ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            icon.setImageResource(hidden ? R.drawable.ic_eye : R.drawable.ic_eye_off);
            input.setSelection(input.length());
        });
    }
}
