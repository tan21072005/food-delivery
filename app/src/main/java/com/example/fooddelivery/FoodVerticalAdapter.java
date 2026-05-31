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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FoodVerticalAdapter extends RecyclerView.Adapter<FoodVerticalAdapter.VH> {

    public interface OnItemClick { void onClick(FoodItem item); }
    public interface OnAddCart   { void onAdd(FoodItem item); }

    private final Context context;
    private final List<FoodItem> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnAddCart   onAddCart;

    public FoodVerticalAdapter(Context ctx, OnItemClick onItemClick, OnAddCart onAddCart) {
        this.context     = ctx;
        this.onItemClick = onItemClick;
        this.onAddCart   = onAddCart;
    }

    public void submitList(List<FoodItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_food_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FoodItem item = items.get(pos);

        h.tvName.setText(item.getName());
        h.tvDesc.setText(item.getDescription());
        h.tvPrice.setText(item.getFormattedPrice());
        h.tvSold.setText(item.getSoldCountLabel());

        Glide.with(context)
                .load(item.getImageUrl())
                .transform(new RoundedCorners(20))
                .placeholder(R.drawable.placeholder_food)
                .into(h.imgFood);

        h.itemView.setOnClickListener(v -> onItemClick.onClick(item));
        h.btnAdd.setOnClickListener(v -> {
            onAddCart.onAdd(item);
            h.btnAdd.animate().scaleX(0.8f).scaleY(0.8f).setDuration(80)
                    .withEndAction(() ->
                            h.btnAdd.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                    .start();
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView  tvName, tvDesc, tvPrice, tvSold;
        FloatingActionButton btnAdd;

        VH(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            tvName  = v.findViewById(R.id.tvFoodName);
            tvDesc  = v.findViewById(R.id.tvDescription);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvSold  = v.findViewById(R.id.tvSoldCount);
            btnAdd  = v.findViewById(R.id.btnAddToCart);
        }
    }
}