package com.example.fooddelivery.ui.order.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.Order;

import java.util.List;

/**
 * Adapter dÃ¹ng chung cho cáº£ 3 tab.
 * Tá»± Ä‘á»™ng chá»n layout Ä‘Ãºng theo status cá»§a tá»«ng tab:
 *   "pending"   â†’ item_order_pending.xml   (badge xÃ¡m, khÃ´ng cÃ³ Äáº·t láº¡i)
 *   "completed" â†’ item_order_completed.xml (badge xanh + nÃºt Äáº·t láº¡i vÃ ng)
 *   "cancelled" â†’ item_order_cancelled.xml (badge xÃ¡m + nÃºt Äáº·t láº¡i vÃ ng)
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    // ViewType constants
    private static final int TYPE_PENDING   = 0;
    private static final int TYPE_COMPLETED = 1;
    private static final int TYPE_CANCELLED = 2;

    public interface OnOrderActionListener {
        void onViewDetailClick(Order order);
        void onReorderClick(Order order);   // chá»‰ gá»i vá»›i completed & cancelled
    }

    private final Context context;
    private List<Order> orderList;
    private final String tabStatus;            // "pending" | "completed" | "cancelled"
    private final OnOrderActionListener listener;

    public OrderAdapter(Context context, List<Order> orderList,
                        String tabStatus, OnOrderActionListener listener) {
        this.context   = context;
        this.orderList = orderList;
        this.tabStatus = tabStatus;
        this.listener  = listener;
    }

    // â”€â”€â”€ ViewType â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    public int getItemViewType(int position) {
        switch (tabStatus) {
            case "completed": return TYPE_COMPLETED;
            case "cancelled": return TYPE_CANCELLED;
            default:          return TYPE_PENDING;
        }
    }

    // â”€â”€â”€ onCreateViewHolder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case TYPE_COMPLETED: layoutRes = R.layout.order_item_completed; break;
            case TYPE_CANCELLED: layoutRes = R.layout.order_item_cancelled; break;
            default:             layoutRes = R.layout.order_item_pending;   break;
        }
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new OrderViewHolder(view, viewType);
    }

    // â”€â”€â”€ onBindViewHolder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // áº¢nh mÃ³n
        if (order.getFoodImageResId() != 0)
            holder.imgFood.setImageResource(order.getFoodImageResId());
        else
            holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);

        // Text
        holder.tvFoodName.setText(order.getFoodName());
        holder.tvTableInfo.setText(order.getTableInfo());
        holder.tvQuantityPrice.setText(order.getQuantityAndPrice());
        holder.tvTime.setText(order.getTimeLabel());

        // Xem chi tiáº¿t
        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailClick(order);
        });

        // Äáº·t láº¡i (chá»‰ cÃ³ á»Ÿ completed & cancelled)
        if (holder.btnReorder != null) {
            holder.btnReorder.setOnClickListener(v -> {
                if (listener != null) listener.onReorderClick(order);
            });
        }
    }

    @Override
    public int getItemCount() { return orderList != null ? orderList.size() : 0; }

    public void updateData(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    // â”€â”€â”€ ViewHolder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView     imgFood;
        TextView      tvFoodName, tvTableInfo, tvQuantityPrice, tvTime;
        LinearLayout  btnViewDetail;
        TextView      btnReorder;   // null cho tab "pending"

        OrderViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            imgFood         = itemView.findViewById(R.id.imgFood);
            tvFoodName      = itemView.findViewById(R.id.tvFoodName);
            tvTableInfo     = itemView.findViewById(R.id.tvTableInfo);
            tvQuantityPrice = itemView.findViewById(R.id.tvQuantityPrice);
            tvTime          = itemView.findViewById(R.id.tvTime);
            btnViewDetail   = itemView.findViewById(R.id.btnViewDetail);

            // btnReorder chá»‰ tá»“n táº¡i trong layout completed & cancelled
            if (viewType == TYPE_COMPLETED || viewType == TYPE_CANCELLED) {
                btnReorder = itemView.findViewById(R.id.btnReorder);
            }
        }
    }
}
