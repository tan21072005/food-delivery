package com.example.fooddelivery.ui.cart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;

import java.util.List;

public class CartBottomSheetAdapter
        extends RecyclerView.Adapter<CartBottomSheetAdapter.VH> {

    public interface Listener {
        void onIncrease(LocalCart.CartEntry entry);
        void onDecrease(LocalCart.CartEntry entry);
    }

    private final Context context;
    private List<LocalCart.CartEntry> entries;
    private final Listener listener;

    public CartBottomSheetAdapter(Context context,
                                  List<LocalCart.CartEntry> entries,
                                  Listener listener) {
        this.context = context;
        this.entries = entries;
        this.listener = listener;
    }

    public void updateData(List<LocalCart.CartEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.cart_bottom_sheet_item, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LocalCart.CartEntry entry = entries.get(position);
        holder.tvName.setText(entry.item.getName());
        holder.tvDesc.setText(entry.item.getDescription() != null
                ? entry.item.getDescription()
                : "Tùy chọn mặc định");
        holder.tvPrice.setText(entry.item.getFormattedPrice());
        holder.tvQty.setText(String.valueOf(entry.quantity));

        if (entry.item.getImageResId() != 0) {
            holder.ivItemImage.setImageResource(entry.item.getImageResId());
        } else {
            holder.ivItemImage.setImageResource(R.drawable.placeholder_food);
        }

        holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(entry));
        holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(entry));
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvName;
        TextView tvDesc;
        TextView tvPrice;
        TextView tvQty;
        TextView btnIncrease;
        TextView btnDecrease;

        VH(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDesc = itemView.findViewById(R.id.tvItemDesc);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
