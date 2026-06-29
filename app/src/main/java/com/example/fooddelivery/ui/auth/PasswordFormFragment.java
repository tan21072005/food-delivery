package com.example.fooddelivery.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.profile.AccountMenuFragment;

public class PasswordFormFragment extends Fragment {

    public static final String ARG_MODE = "MODE";
    public static final String MODE_CREATE = "CREATE";
    public static final String MODE_CHANGE = "CHANGE";
    public static final String MODE_RESET = "RESET";

    private String currentMode = MODE_CHANGE;

    private TextView tvPasswordTitle;
    private View layoutOldPassword;
    private EditText edOldPassword, edNewPassword, edConfirmPassword;
    private Button btnSubmitPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentMode = getArguments().getString(ARG_MODE, MODE_CHANGE);
        }

        tvPasswordTitle = view.findViewById(R.id.tvPasswordTitle);
        layoutOldPassword = view.findViewById(R.id.layoutOldPassword);
        edOldPassword = view.findViewById(R.id.edOldPassword);
        edNewPassword = view.findViewById(R.id.edNewPassword);
        edConfirmPassword = view.findViewById(R.id.edConfirmPassword);
        btnSubmitPassword = view.findViewById(R.id.btnSubmitPassword);

        setupUIForMode();

        btnSubmitPassword.setOnClickListener(v -> handleSubmit());
    }

    private void setupUIForMode() {
        switch (currentMode) {
            case MODE_CREATE:
                tvPasswordTitle.setText("Tạo mật khẩu");
                layoutOldPassword.setVisibility(View.GONE);
                btnSubmitPassword.setText("Tạo tài khoản");
                break;
            case MODE_CHANGE:
                tvPasswordTitle.setText("Thay đổi mật khẩu");
                layoutOldPassword.setVisibility(View.VISIBLE);
                btnSubmitPassword.setText("Cập nhật");
                break;
            case MODE_RESET:
                tvPasswordTitle.setText("Đặt lại mật khẩu");
                layoutOldPassword.setVisibility(View.GONE);
                btnSubmitPassword.setText("Xác nhận");
                break;
        }
    }

    private void handleSubmit() {
        String oldPass = edOldPassword.getText().toString().trim();
        String newPass = edNewPassword.getText().toString().trim();
        String confirmPass = edConfirmPassword.getText().toString().trim();

        if (currentMode.equals(MODE_CHANGE) && TextUtils.isEmpty(oldPass)) {
            edOldPassword.setError("Vui lòng nhập mật khẩu cũ");
            return;
        }
        if (TextUtils.isEmpty(newPass)) {
            edNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (newPass.length() < 6) {
            edNewPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            edConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        NavController controller = NavHostFragment.findNavController(this);
        if (MODE_CHANGE.equals(currentMode)) {
            NavBackStackEntry previous = controller.getPreviousBackStackEntry();
            if (previous != null) {
                previous.getSavedStateHandle().set(
                        AccountMenuFragment.PASSWORD_CHANGED_RESULT, true);
            }
            controller.popBackStack();
            return;
        }

        Toast.makeText(getContext(), "Xử lý thành công!", Toast.LENGTH_SHORT).show();
        controller.popBackStack();
    }
}
