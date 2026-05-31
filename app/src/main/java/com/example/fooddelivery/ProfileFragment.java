package com.example.fooddelivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment
        implements ProfileMenuAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProfileMenuAdapter adapter;
    private List<ProfileMenuItem> menuItems;
    private String currentUserName = "Trần Nhật Tân";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(null);

        buildMenuList();
        adapter = new ProfileMenuAdapter(menuItems, this);
        recyclerView.setAdapter(adapter);
    }

    private void buildMenuList() {
        menuItems = new ArrayList<>();

        // Header
        menuItems.add(new ProfileMenuItem());

        // Section: Tài khoản
        menuItems.add(new ProfileMenuItem("Tài khoản"));
        menuItems.add(new ProfileMenuItem("tai_khoan",      android.R.drawable.ic_menu_myplaces, "Tài khoản",                true,  true));
        menuItems.add(new ProfileMenuItem("quyen_rieng_tu", android.R.drawable.ic_lock_lock,     "Quyền riêng tư",           true,  true));
        menuItems.add(new ProfileMenuItem("bao_mat",        android.R.drawable.ic_menu_call,     "Bảo mật & quyền",          true,  true));
        menuItems.add(new ProfileMenuItem("chia_se",        android.R.drawable.ic_menu_share,    "Chia sẻ hồ sơ",            true,  false));

        // Section: Khuyến mãi
        menuItems.add(new ProfileMenuItem("Khuyến mãi"));
        menuItems.add(new ProfileMenuItem("khuyen_mai",     android.R.drawable.ic_menu_call,     "Khuyến mãi",               true,  true));
        menuItems.add(new ProfileMenuItem("goi_tiet_kiem",  android.R.drawable.ic_menu_today,    "Gói tiết kiệm",            false, true));
        menuItems.add(new ProfileMenuItem("thanh_toan",     android.R.drawable.ic_menu_agenda,   "Thanh toán",               false, false));

        // Section: Hỗ trợ
        menuItems.add(new ProfileMenuItem("Hỗ trợ"));
        menuItems.add(new ProfileMenuItem("ho_tro",         android.R.drawable.ic_menu_call,     "Hỗ trợ",                   true,  true));
        menuItems.add(new ProfileMenuItem("dieu_khoan",     android.R.drawable.ic_menu_agenda,   "Điều khoản và chính sách", false, false));
        // Section: Hỗ trợ
        menuItems.add(new ProfileMenuItem("Đăng nhập"));
        menuItems.add(new ProfileMenuItem("chuyen_doi_tai_khoan",  R.drawable.ic_change_account,     "Hỗ trợ",                   true,  true));
        menuItems.add(new ProfileMenuItem("dang_xuat",             R.drawable.ic_logout,   "Đăng xuất", true, true));
    }

    // ── OnItemClickListener ──
    @Override
    public void onMenuItemClick(String itemId) {
        switch (itemId) {
            case "tai_khoan":
                // Dùng Navigation Component để chuyển màn
                // Navigation.findNavController(requireView())
                //         .navigate(R.id.action_profileFragment_to_accountFragment);
                break;
            case "quyen_rieng_tu":
                break;
            case "bao_mat":
                break;
            case "chia_se":
                break;
            case "khuyen_mai":
                break;
            case "goi_tiet_kiem":
                break;
            case "thanh_toan":
                break;
            case "ho_tro":
                break;
            case "dieu_khoan":
                break;
        }
    }

    @Override
    public void onSwitchAccountClick() {
        openLogoutSheet();
    }

    @Override
    public void onEditProfileClick() {
        // Navigation.findNavController(requireView())
        //         .navigate(R.id.action_profileFragment_to_accountInfoFragment);
    }

    @Override
    public void onMoreClick() {
        openLogoutSheet();
    }

    private void openLogoutSheet() {
        LogoutBottomSheet sheet = new LogoutBottomSheet();
        sheet.setListener(new LogoutBottomSheet.Listener() {
            @Override
            public void onSwitchAccount() {
                openSwitchAccountSheet();
            }
            @Override
            public void onLogout() {
                // TODO: xóa token, về LoginActivity
            }
        });
        sheet.show(getParentFragmentManager(), "LogoutSheet");
    }

    private void openSwitchAccountSheet() {
        SwitchAccountBottomSheet sheet = new SwitchAccountBottomSheet();
        sheet.setCurrentUser(currentUserName);
        sheet.setListener(userName -> {
            currentUserName = userName;
            adapter.notifyItemChanged(0);
        });
        sheet.show(getParentFragmentManager(), "SwitchAccountSheet");
    }
}