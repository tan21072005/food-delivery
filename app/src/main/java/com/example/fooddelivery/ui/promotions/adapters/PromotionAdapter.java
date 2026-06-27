package com.example.fooddelivery.ui.promotions.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.promotions.model.PromotionItem;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {

    public interface OnDangKyClickListener {
        void onDangKyClick(PromotionItem item);
    }

    private final Context context;
    private final List<PromotionItem> items;
    private OnDangKyClickListener listener;

    public PromotionAdapter(Context context, List<PromotionItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnDangKyClickListener(OnDangKyClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_promotion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PromotionItem item = items.get(position);
        holder.tvAvatar.setText(item.getEmoji());
        holder.tvName.setText(item.getName());
        holder.tvValue.setText(item.getValue());
        holder.tvQty.setText(item.getQuantity());
        holder.tvDesc.setText(item.getDescription());
        holder.tvPrice.setText(item.getPrice());
        holder.btnDangKy.setOnClickListener(v -> {
            if (listener != null) listener.onDangKyClick(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvValue, tvQty, tvDesc, tvPrice;
        Button btnDangKy;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvPromoAvatar);
            tvName   = itemView.findViewById(R.id.tvPromoName);
            tvValue  = itemView.findViewById(R.id.tvPromoValue);
            tvQty    = itemView.findViewById(R.id.tvPromoQty);
            tvDesc   = itemView.findViewById(R.id.tvPromoDesc);
            tvPrice  = itemView.findViewById(R.id.tvPromoPrice);
            btnDangKy = itemView.findViewById(R.id.btnDangKy);
        }
    }
}
