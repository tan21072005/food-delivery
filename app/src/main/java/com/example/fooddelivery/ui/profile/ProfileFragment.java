package com.example.fooddelivery.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import androidx.lifecycle.ViewModelProvider;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.ui.auth.AuthActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar, btnEditProfile;
    private TextView tvUserName;
    private ProfileViewModel profileViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        tvUserName = view.findViewById(R.id.tvUserName);

        tvUserName.setText(profileViewModel.getUserName());

        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Cập nhật tên từ Supabase
                String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                        ? user.getFullName()
                        : user.getUsername();
                if ((displayName == null || displayName.isEmpty()) && user.getEmail() != null) {
                    displayName = user.getEmail();
                }
                if (displayName != null && !displayName.isEmpty()) {
                    tvUserName.setText(displayName);
                }

                // Cập nhật Avatar
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(imgAvatar);
                }
            }
        });

        profileViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getUploadStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getUploadSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.loadUserInfo();

        // 1. Đăng ký nhận kết quả chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadAvatar(imageUri);
                        }
                    }
                }
        );

        // 2. Bắt sự kiện bấm vào nút sửa ảnh
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // 3. Xử lý "Chuyển đổi tài khoản"
        View btnSwitchAccountItem = view.findViewById(R.id.btnSwitchAccountItem);
        if (btnSwitchAccountItem != null) {
            btnSwitchAccountItem.setOnClickListener(v -> {
                SwitchAccountBottomSheet sheet = new SwitchAccountBottomSheet();
                sheet.show(getChildFragmentManager(), SwitchAccountBottomSheet.TAG);
            });
        }

        // 4. Xử lý "Đăng xuất"
        View btnLogoutItem = view.findViewById(R.id.btnLogoutItem);
        if (btnLogoutItem != null) {
            btnLogoutItem.setOnClickListener(v -> {
                LogoutBottomSheet sheet = new LogoutBottomSheet(
                        () -> {
                            new SessionManager(requireContext()).clearSession();
                            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireActivity(), AuthActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        },
                        () -> {
                            SwitchAccountBottomSheet switchSheet = new SwitchAccountBottomSheet();
                            switchSheet.show(getChildFragmentManager(), SwitchAccountBottomSheet.TAG);
                        }
                );
                sheet.show(getChildFragmentManager(), LogoutBottomSheet.TAG);
            });
        }
    }

    private void uploadAvatar(Uri imageUri) {
        try {
            // Hiển thị ảnh tạm thời
            imgAvatar.setImageURI(imageUri);

            // Đọc file thành mảng byte
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] fileBytes = byteBuffer.toByteArray();

            // Cấu hình request body chuẩn MIME type (image/jpeg hoặc image/png)
            String mimeType = requireContext().getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), fileBytes);
            String fileName = "user_avatar_" + System.currentTimeMillis() + ".jpg";

            profileViewModel.uploadAvatar(requestBody, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file!", Toast.LENGTH_SHORT).show();
        }
    }
}
