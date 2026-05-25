package com.example.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.adapter.SettingAdapter;
import com.example.app.model.SettingItem;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    RecyclerView recyclerSetting;
    SettingAdapter adapter;
    List<SettingItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerSetting =
                findViewById(R.id.recyclerSetting);

        recyclerSetting.setLayoutManager(
                new LinearLayoutManager(this));

        list = new ArrayList<>();

        list.add(new SettingItem(
                R.drawable.ic_account,
                "Tài khoản",
                AccountActivity.class));

        list.add(new SettingItem(
                R.drawable.ic_privacy,
                "Quyền riêng tư",
                PrivacyActivity.class));

        list.add(new SettingItem(
                R.drawable.ic_security,
                "Bảo mật & quyền",
                SecurityActivity.class));

        list.add(new SettingItem(
                R.drawable.ic_share,
                "Chia sẻ hồ sơ",
                ShareActivity.class));

        list.add(new SettingItem(
                R.drawable.ic_sale,
                "Khuyến mãi",
                PromotionActivity.class));

        list.add(new SettingItem(
                R.drawable.ic_payment,
                "Thanh toán",
                PaymentActivity.class));

        adapter = new SettingAdapter(this, list);
        recyclerSetting.setAdapter(adapter);
    }
}