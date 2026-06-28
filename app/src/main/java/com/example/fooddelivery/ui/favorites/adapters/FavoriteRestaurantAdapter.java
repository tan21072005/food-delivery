package com.example.fooddelivery.ui.favorites.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.favorites.model.FavoriteRestaurant;
import com.example.fooddelivery.ui.favorites.model.RestaurantSuggestion;

import java.util.List;
import java.util.Set;

public class FavoriteRestaurantAdapter extends RecyclerView.Adapter<FavoriteRestaurantAdapter.Holder> {
    public interface Listener { void onToggle(String id); }
    private final List<RestaurantSuggestion> items;
    private final Listener listener;
    private Set<String> selected;
    public FavoriteRestaurantAdapter(List<RestaurantSuggestion> items, Set<String> selected, Listener listener) { this.items = items; this.selected = selected; this.listener = listener; }
    public void setSelected(Set<String> values) { selected = values; notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_restaurant, parent, false)); }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) { holder.bind(items.get(position)); }
    @Override public int getItemCount() { return items.size(); }

    final class Holder extends RecyclerView.ViewHolder {
        final ImageView image; final TextView name, meta, distance, action;
        Holder(View view) { super(view); image = view.findViewById(R.id.restaurantImage); name = view.findViewById(R.id.restaurantName); meta = view.findViewById(R.id.restaurantMeta); distance = view.findViewById(R.id.restaurantDistance); action = view.findViewById(R.id.selectionAction); }
        void bind(RestaurantSuggestion suggestion) {
            FavoriteRestaurant item = suggestion.getRestaurant();
            boolean isSelected = selected.contains(item.getId());
            image.setImageResource(item.getImageRes()); name.setText(item.getName());
            meta.setText("⭐ " + item.getRating() + "   •   " + suggestion.getCompletedOrderCount() + " đơn đã hoàn thành");
            distance.setText("📍 " + item.getDistance());
            action.setText(isSelected ? "✓  Đã thêm vào bộ sưu tập" : "+  Thêm vào bộ sưu tập");
            action.setTextColor(Color.parseColor(isSelected ? "#1F9D55" : "#20B8C2"));
            itemView.setSelected(isSelected); itemView.setOnClickListener(v -> listener.onToggle(item.getId()));
        }
    }
}
