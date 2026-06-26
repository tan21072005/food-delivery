package com.example.fooddelivery.ui.cart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;

import java.util.List;

/**
 * Adapter cho danh sách món trong CartBottomSheet.
 */
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
        this.context  = context;
        this.entries  = entries;
        this.listener = listener;
    }

    public void updateData(List<LocalCart.CartEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.cart_bottom_sheet_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LocalCart.CartEntry entry = entries.get(position);
        h.tvName.setText(entry.item.getName());
        h.tvDesc.setText(entry.item.getDescription() != null
                ? entry.item.getDescription() : "Nhiều bún, không thịt");
        h.tvPrice.setText(entry.item.getFormattedPrice());
        h.tvQty.setText(String.valueOf(entry.quantity));

        h.btnIncrease.setOnClickListener(v -> listener.onIncrease(entry));
        h.btnDecrease.setOnClickListener(v -> listener.onDecrease(entry));
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvQty, btnIncrease, btnDecrease;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvItemName);
            tvDesc      = itemView.findViewById(R.id.tvItemDesc);
            tvPrice     = itemView.findViewById(R.id.tvItemPrice);
            tvQty       = itemView.findViewById(R.id.tvQty);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
