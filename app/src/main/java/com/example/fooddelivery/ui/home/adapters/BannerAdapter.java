package com.example.fooddelivery.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.fooddelivery.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerVH> {

    private final Context context;
    private final List<String> imageUrls; // URL hoặc drawable res

    public BannerAdapter(Context context, List<String> imageUrls) {
        this.context   = context;
        this.imageUrls = imageUrls;
    }

    @NonNull @Override
    public BannerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.home_item_banner, parent, false);
        return new BannerVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerVH h, int position) {
        // Lấy index thật (vì dùng vòng lặp vô tận)
        int realIndex = position % imageUrls.size();
        String url = imageUrls.get(realIndex);

        // Nếu url là dạng relative path (vd: banner1.png) từ Database, nối thêm Base URL
        if (url != null && !url.startsWith("http")) {
            url = com.example.fooddelivery.utils.Constants.STORAGE_BASE_URL + "banners/" + url;
        }

        Glide.with(context)
                .load(url)
                .transform(new RoundedCorners(24))
                .placeholder(R.drawable.placeholder_banner)
                .into(h.imgBanner);
    }

    // Trả về rất lớn để trượt vô tận (infinite loop)
    @Override
    public int getItemCount() {
        return imageUrls.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    // Trả về vị trí bắt đầu ở giữa để scroll được cả 2 hướng
    public int getStartPosition() {
        return (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % imageUrls.size());
    }

    static class BannerVH extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        BannerVH(@NonNull View v) {
            super(v);
            imgBanner = v.findViewById(R.id.imgBanner);
        }
    }
}