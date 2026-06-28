package com.example.fooddelivery.ui.favorites.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.favorites.data.FavoriteRestaurantCatalog;
import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;
import com.example.fooddelivery.ui.favorites.model.FavoriteRestaurant;

import java.util.ArrayList;
import java.util.List;

public class FavoriteCollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COLLECTION = 0;
    private static final int TYPE_ADD = 1;
    private static final int TYPE_SYSTEM = 2;
    public interface Listener {
        void onCollectionClick(String id);
        void onRenameClick(String id);
        void onDeleteClick(String id);
        void onAddClick();
    }
    private final Listener listener;
    private final List<FavoriteCollection> items = new ArrayList<>();

    public FavoriteCollectionAdapter(Listener listener) { this.listener = listener; }
    public void submitList(List<FavoriteCollection> values) { items.clear(); items.addAll(values); notifyDataSetChanged(); }
    @Override public int getItemCount() { return items.size() + 2; }
    @Override public int getItemViewType(int position) {
        if (position == 0) return TYPE_SYSTEM;
        return position == items.size() + 1 ? TYPE_ADD : TYPE_COLLECTION;
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        int layout = type == TYPE_ADD ? R.layout.item_add_favorite_collection : R.layout.item_favorite_collection;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return type == TYPE_ADD ? new AddHolder(view) : new CollectionHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddHolder) holder.itemView.setOnClickListener(v -> listener.onAddClick());
        else if (getItemViewType(position) == TYPE_SYSTEM) ((CollectionHolder) holder).bindSystem();
        else ((CollectionHolder) holder).bind(items.get(position - 1));
    }

    final class CollectionHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView name;
        private final TextView count;
        private final ImageButton menu;
        CollectionHolder(View view) { super(view); image = view.findViewById(R.id.collectionImage); name = view.findViewById(R.id.collectionName); count = view.findViewById(R.id.collectionCount); menu = view.findViewById(R.id.collectionMenu); }
        void bindSystem() {
            name.setText("Yêu thích"); count.setText("0 quán");
            image.setImageResource(R.drawable.placeholder_banner);
            menu.setVisibility(View.GONE); itemView.setOnClickListener(null);
        }
        void bind(FavoriteCollection item) {
            name.setText(item.getName());
            count.setText(item.getRestaurantIds().size() + " quán");
            FavoriteRestaurant first = item.getRestaurantIds().isEmpty() ? null : FavoriteRestaurantCatalog.findById(item.getRestaurantIds().get(0));
            image.setImageResource(first == null ? R.drawable.placeholder_banner : first.getImageRes());
            menu.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(v -> listener.onCollectionClick(item.getId()));
            menu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.favorite_collection_actions);
                popup.setOnMenuItemClickListener(action -> {
                    if (action.getItemId() == R.id.action_rename_collection) listener.onRenameClick(item.getId());
                    else if (action.getItemId() == R.id.action_delete_collection) listener.onDeleteClick(item.getId());
                    return true;
                });
                popup.show();
            });
        }
    }
    static final class AddHolder extends RecyclerView.ViewHolder { AddHolder(View view) { super(view); } }
}
