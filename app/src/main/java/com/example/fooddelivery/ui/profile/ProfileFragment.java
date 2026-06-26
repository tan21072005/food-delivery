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
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;
import com.example.fooddelivery.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar, btnEditProfile;
    private TextView tvUserName;
    private SessionManager sessionManager;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiService = SupabaseClient.getInstance(requireContext()).create(ApiService.class);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        tvUserName = view.findViewById(R.id.tvUserName);

        loadUserInfo();

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
    }

    private void loadUserInfo() {
        tvUserName.setText(sessionManager.getUserName());
        String userIdStr = String.valueOf(sessionManager.getUserId());
        
        apiService.getUserById("eq." + userIdStr).enqueue(new Callback<java.util.List<User>>() {
            @Override
            public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(imgAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<java.util.List<User>> call, Throwable t) {}
        });
    }

    private void uploadAvatar(Uri imageUri) {
        try {
            // Hiển thị ảnh tạm thời
            imgAvatar.setImageURI(imageUri);
            Toast.makeText(getContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

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
            String fileName = "user_" + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg";

            // Gọi API Upload
            apiService.uploadFile("avatars", fileName, requestBody).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Cập nhật Database với URL ảnh mới
                        String publicUrl = Constants.SUPABASE_URL + "/storage/v1/object/public/avatars/" + fileName;
                        updateUserAvatarInDb(publicUrl);
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserAvatarInDb(String publicUrl) {
        User updateData = new User();
        updateData.setAvatarUrl(publicUrl);
        String eqId = "eq." + sessionManager.getUserId();

        apiService.updateUser(eqId, updateData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
