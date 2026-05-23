package com.example.fooddelivery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
import com.example.fooddelivery.FoodCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnCategoryClick {
        void onClick(FoodCategory category);
    }

    private final Context context;
    private final List<FoodCategory> items = new ArrayList<>();
    private final OnCategoryClick listener;
    private int selectedPosition = 0;

    public CategoryAdapter(Context context, OnCategoryClick listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void submitList(List<FoodCategory> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FoodCategory cat = items.get(position);
        h.tvName.setText(cat.getName());

        // Load icon từ URL
        Glide.with(context)
                .load(cat.getIconUrl())
                .placeholder(R.drawable.placeholder_food)
                .circleCrop()
                .into(h.imgIcon);

        // Highlight danh mục đang chọn
        h.imgIcon.setAlpha(selectedPosition == position ? 1.0f : 0.6f);
//        h.tvName.setTextColor(selectedPosition == position
//                ? context.getColor(R.color.orange_primary)
//                : context.getColor(R.color.text_secondary));

        h.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = h.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onClick(cat);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView  tvName;
        VH(@NonNull View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgCategory);
            tvName  = v.findViewById(R.id.tvCategoryName);
        }
    }
}