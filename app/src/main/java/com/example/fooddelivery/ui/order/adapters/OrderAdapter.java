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
        void onReorderClick(Order order);   // cho completed & cancelled
        void onReviewClick(Order order);    // cho completed
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
        if (holder.tvTableInfo != null) {
            holder.tvTableInfo.setText(order.getTableInfo());
        }
        if (holder.tvQuantityPrice != null) {
            holder.tvQuantityPrice.setText(order.getQuantityAndPrice());
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText(order.getTimeLabel());
        }
        if (holder.tvStatusBadge != null) {
            holder.tvStatusBadge.setText(getStatusLabel(order.getStatus()));
        }

        // Xem chi tiết / Review container logic
        if (holder.btnViewDetail != null) {
            holder.btnViewDetail.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailClick(order);
            });
        }
        
        if (holder.btnReview != null) {
            if (order.isReviewed()) {
                holder.btnReview.setVisibility(View.GONE);
                if (holder.btnViewReview != null) holder.btnViewReview.setVisibility(View.VISIBLE);
            } else {
                holder.btnReview.setVisibility(View.VISIBLE);
                if (holder.btnViewReview != null) holder.btnViewReview.setVisibility(View.GONE);
            }

            View.OnClickListener reviewClickListener = v -> {
                if (listener != null && !order.isReviewed()) listener.onReviewClick(order);
            };
            holder.btnReview.setOnClickListener(reviewClickListener);
            
            // Đảm bảo click vào chữ hay icon cũng đều ăn sự kiện
            TextView tvReviewText = holder.itemView.findViewById(R.id.tvReviewText);
            if (tvReviewText != null) tvReviewText.setOnClickListener(reviewClickListener);
            ImageView ivReviewArrow = holder.itemView.findViewById(R.id.ivReviewArrow);
            if (ivReviewArrow != null) ivReviewArrow.setOnClickListener(reviewClickListener);
            if (holder.btnViewReview != null) {
                holder.btnViewReview.setOnClickListener(v -> {
                     if (listener != null) listener.onReviewClick(order);
                });
            }
        }

        // Đặt lại (chỉ có ở completed & cancelled)
        if (holder.btnReorder != null) {
            holder.btnReorder.setOnClickListener(v -> {
                if (listener != null) listener.onReorderClick(order);
            });
        }

        // Ngày giờ
        if (holder.tvOrderDate != null) {
            holder.tvOrderDate.setText(order.getOrderDate());
        }
    }

    @Override
    public int getItemCount() { return orderList != null ? orderList.size() : 0; }

    public void updateData(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "confirmed": return "Đã xác nhận";
            case "preparing": return "Đang chuẩn bị";
            case "delivering": return "Đang giao";
            case "completed": return "Đã hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return "Đang chờ";
        }
    }

    // â”€â”€â”€ ViewHolder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView     imgFood;
        TextView      tvFoodName, tvTableInfo, tvQuantityPrice, tvTime, tvOrderDate, tvStatusBadge;
        LinearLayout  btnViewDetail, btnReview, btnViewReview;
        TextView      btnReorder;   // null cho tab "pending"

        OrderViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            imgFood         = itemView.findViewById(R.id.imgFood);
            tvFoodName      = itemView.findViewById(R.id.tvFoodName);
            tvTableInfo     = itemView.findViewById(R.id.tvTableInfo);
            tvQuantityPrice = itemView.findViewById(R.id.tvQuantityPrice);
            tvTime          = itemView.findViewById(R.id.tvTime);
            tvStatusBadge   = itemView.findViewById(R.id.tvStatusBadge);
            btnViewDetail   = itemView.findViewById(R.id.btnViewDetail);

            // Các layout mới có thêm tvOrderDate
            tvOrderDate     = itemView.findViewById(R.id.tvOrderDate);

            // btnReorder chỉ tồn tại trong layout completed & cancelled
            if (viewType == TYPE_COMPLETED || viewType == TYPE_CANCELLED) {
                btnReorder = itemView.findViewById(R.id.btnReorder);
            }
            if (viewType == TYPE_COMPLETED) {
                btnReview = itemView.findViewById(R.id.btnReview);
                btnViewReview = itemView.findViewById(R.id.btnViewReview);
            }
        }
    }
}
