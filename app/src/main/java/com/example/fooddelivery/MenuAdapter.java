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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.fooddelivery.R;
//import com.example.fooddelivery.model.FoodItem;
import com.example.fooddelivery.FoodItem;


import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.FoodViewHolder> {

    public interface OnItemClickListener {
        void onFoodClick(FoodItem item);
        void onAddToCartClick(FoodItem item);
    }

    private final Context context;
    private List<FoodItem> foodList;
    private List<FoodItem> foodListFull; // for search filtering
    private OnItemClickListener listener;

    public MenuAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = new ArrayList<>(foodList);
        this.foodListFull = new ArrayList<>(foodList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Filter list by name keyword */
    public void filter(String query) {
        foodList.clear();
        if (query == null || query.trim().isEmpty()) {
            foodList.addAll(foodListFull);
        } else {
            String lower = query.toLowerCase().trim();
            for (FoodItem item : foodListFull) {
                if (item.getName().toLowerCase().contains(lower)
                        || item.getDescription().toLowerCase().contains(lower)) {
                    foodList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Replace the full data set (e.g. after API response) */
    public void setData(List<FoodItem> newList) {
        foodListFull.clear();
        foodListFull.addAll(newList);
        foodList.clear();
        foodList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_menu_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);

        holder.tvFoodName.setText(item.getName());
        holder.tvFoodDescription.setText(item.getDescription());
        holder.tvSoldCount.setText(item.getSoldCountLabel());
        holder.tvPrice.setText(item.getFormattedPrice());

        // Load image: prefer URL (Glide) over local drawable
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(24))
                            .placeholder(R.drawable.placeholder_food)
                            .error(R.drawable.placeholder_food))
                    .into(holder.imgFood);
        } else if (item.getImageResId() != 0) {
            Glide.with(context)
                    .load(item.getImageResId())
                    .apply(new RequestOptions().transform(new RoundedCorners(24)))
                    .into(holder.imgFood);
        }

        // Clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(item);
        });
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(item);
            // Quick scale animation feedback
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
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvFoodDescription, tvSoldCount, tvPrice;
        FloatingActionButton btnAddToCart;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvSoldCount = itemView.findViewById(R.id.tvSoldCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}