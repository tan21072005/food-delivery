package com.example.fooddelivery.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fooddelivery.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        view.findViewById(R.id.rowName).setOnClickListener(v -> showTextDialog(
                "Chỉnh sửa tên", "Tên", tvNameValue.getText().toString(),
                AccountInfoViewModel.AccountField.NAME));
        view.findViewById(R.id.rowPhone).setOnClickListener(v -> showTextDialog(
                "Chỉnh sửa số điện thoại", "XXXXXXXXXX", tvPhoneValue.getText().toString(),
                AccountInfoViewModel.AccountField.PHONE));
        view.findViewById(R.id.rowEmail).setOnClickListener(v -> showTextDialog(
                "Chỉnh sửa email", "Email@gmail.com", tvEmailValue.getText().toString(),
                AccountInfoViewModel.AccountField.EMAIL));
        view.findViewById(R.id.rowBirthday).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.rowCountry).setOnClickListener(v -> showTextDialog(
                "Chỉnh sửa quốc gia", "Quốc gia", tvCountryValue.getText().toString(),
                AccountInfoViewModel.AccountField.COUNTRY));

        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            Toast.makeText(requireContext(), result.successful
                    ? "Cập nhật thành công"
                    : "Không thể cập nhật. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            viewModel.clearUpdateResult();
        });
        viewModel.loadAccountInfo();
    }

    private void showTextDialog(String title, String hint, String currentValue,
                                AccountInfoViewModel.AccountField field) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setSingleLine(true);
        input.setText("không có".equals(currentValue) ? "" : currentValue);
        input.setSelection(input.getText().length());
        input.setInputType(inputTypeFor(field));

        int horizontalPadding = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(requireContext());
        container.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        container.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String value = input.getText().toString().trim();
                    String error = validate(field, value);
                    if (error != null) {
                        input.setError(error);
                        return;
                    }
                    viewModel.updateField(field, value);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private int inputTypeFor(AccountInfoViewModel.AccountField field) {
        if (field == AccountInfoViewModel.AccountField.EMAIL) {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        }
        if (field == AccountInfoViewModel.AccountField.PHONE) {
            return InputType.TYPE_CLASS_PHONE;
        }
        return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
    }

    @Nullable
    private String validate(AccountInfoViewModel.AccountField field, String value) {
        if (value.isEmpty()) {
            return "Vui lòng nhập thông tin";
        }
        if (field == AccountInfoViewModel.AccountField.EMAIL
                && !AccountInfoValidator.isValidEmail(value)) {
            return "Email không hợp lệ";
        }
        if (field == AccountInfoViewModel.AccountField.PHONE
                && !AccountInfoValidator.isValidPhone(value)) {
            return "Số điện thoại phải có từ 8 đến 15 chữ số";
        }
        return null;
    }

    private void showDatePicker() {
        Calendar selected = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    String databaseValue = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, day);
                    if (AccountInfoValidator.isValidBirthDate(databaseValue, new Date())) {
                        viewModel.updateField(AccountInfoViewModel.AccountField.BIRTH_DATE,
                                databaseValue);
                    }
                }, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH),
                selected.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.setTitle("Chọn ngày sinh");
        picker.show();
    }

    private void render(AccountInfoViewModel.AccountInfoUiState state) {
        tvNameValue.setText(state.name);
        tvPhoneValue.setText(state.phoneNumber);
        tvEmailValue.setText(state.email);
        tvBirthdayValue.setText(state.birthDate);
        tvCountryValue.setText(state.country);
    }
}
