package com.example.fooddelivery.ui.detail.adapters;

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
import com.bumptech.glide.request.RequestOptions;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class StorefrontAdapter extends RecyclerView.Adapter<StorefrontAdapter.VH> {

    public interface OnItemClickListener {
        void onFoodClick(FoodItem item);
        void onAddToCartClick(FoodItem item);
    }

    private final Context context;
    private final List<FoodItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public StorefrontAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FoodItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.storefront_item_food, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FoodItem item = items.get(position);

        holder.tvFoodName.setText(item.getName());
        holder.tvPrice.setText(item.getFormattedPrice());
        holder.tvSoldCount.setText(item.getSoldCountLabel());

        // Load image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .centerCrop())
                    .into(holder.imgFood);
        } else if (item.getImageResId() != 0) {
            Glide.with(context)
                    .load(item.getImageResId())
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.imgFood);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(item);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(item);
            holder.btnAddToCart.animate()
                    .scaleX(0.8f).scaleY(0.8f).setDuration(100)
                    .withEndAction(() ->
                            holder.btnAddToCart.animate()
                                    .scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvSoldCount, tvPrice;
        FloatingActionButton btnAddToCart;

        VH(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            tvFoodName = v.findViewById(R.id.tvFoodName);
            tvSoldCount = v.findViewById(R.id.tvSoldCount);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnAddToCart = v.findViewById(R.id.btnAddToCart);
        }
    }
}
