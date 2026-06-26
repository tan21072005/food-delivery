package com.example.fooddelivery.ui.home.adapters;

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
import com.example.fooddelivery.data.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class NearbyRestaurantAdapter extends RecyclerView.Adapter<NearbyRestaurantAdapter.ViewHolder> {

    private final Context context;
    private List<FoodItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRestaurantClick(FoodItem item);
    }

    public NearbyRestaurantAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<FoodItem> list) {
        if (list != null) {
            this.items = list;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = items.get(position);

        // Just using food info to fake a restaurant for now
        holder.tvRestaurantName.setText("Quán " + item.getName());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        
        // Mock distance and time
        holder.tvDistance.setText(String.format("%.1f km", 0.5f + position * 0.3f));
        holder.tvTime.setText((10 + position * 2) + " phút");

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .into(holder.ivRestaurantCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRestaurantClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantCover;
        TextView tvRating;
        TextView tvRestaurantName;
        TextView tvDistance;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantCover = itemView.findViewById(R.id.ivRestaurantCover);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
