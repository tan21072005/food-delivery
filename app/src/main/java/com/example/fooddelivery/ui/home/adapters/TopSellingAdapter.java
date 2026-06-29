package com.example.fooddelivery.ui.home.adapters;

import com.example.fooddelivery.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.fooddelivery.data.model.FoodItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TopSellingAdapter extends RecyclerView.Adapter<TopSellingAdapter.VH> {

    public interface OnItemClick { void onClick(FoodItem item); }
    public interface OnAddCart   { void onAdd(FoodItem item); }

    private final Context context;
    private final List<FoodItem> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnAddCart   onAddCart;
    private final boolean showAddButton;

    public TopSellingAdapter(Context ctx, OnItemClick onItemClick, OnAddCart onAddCart) {
        this(ctx, onItemClick, onAddCart, true);
    }

    public TopSellingAdapter(Context ctx, OnItemClick onItemClick, OnAddCart onAddCart, boolean showAddButton) {
        this.context     = ctx;
        this.onItemClick = onItemClick;
        this.onAddCart   = onAddCart;
        this.showAddButton = showAddButton;
    }

    public void submitList(List<FoodItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.home_item_food_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FoodItem item = items.get(pos);

        h.tvName.setText(item.getName());
        h.tvPrice.setText(item.getFormattedPrice());

        Glide.with(context)
                .load(item.getImageUrl())
                .transform(new RoundedCorners(16))
                .placeholder(R.drawable.placeholder_food)
                .into(h.imgFood);

        // Click vào item → mở chi tiết
        h.itemView.setOnClickListener(v -> onItemClick.onClick(item));

        // Click nút + → thêm giỏ hàng
        h.btnAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
        h.btnAdd.setOnClickListener(showAddButton ? v -> {
            if (onAddCart != null) {
                onAddCart.onAdd(item);
                animateButton(h.btnAdd);
            }
        } : null);
    }

    private void animateButton(View btn) {
        btn.animate().scaleX(0.8f).scaleY(0.8f).setDuration(80)
                .withEndAction(() ->
                        btn.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView  tvName, tvPrice;
        FloatingActionButton btnAdd;

        VH(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            tvName  = v.findViewById(R.id.tvFoodName);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnAdd  = v.findViewById(R.id.btnAddToCart);
        }
    }
}
