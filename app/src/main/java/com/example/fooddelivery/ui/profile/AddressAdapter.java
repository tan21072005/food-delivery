package com.example.fooddelivery.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.DeliveryAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

    private final Context context;
    private final List<DeliveryAddress> items = new ArrayList<>();
    private OnAddressClickListener listener;
    private OnAddressActionListener editListener;

    public AddressAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    public void setEditListener(OnAddressActionListener editListener) {
        this.editListener = editListener;
    }

    public void submitList(List<DeliveryAddress> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DeliveryAddress item = items.get(position);
        holder.tvAddressLabel.setText(item.getDisplayLabel());
        holder.tvAddressDetail.setText(item.getFullAddress());
        holder.tvUserInfo.setText(item.getRecipientLine());
        holder.tvDefaultTag.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);

        String type = item.getType() == null ? "" : item.getType().toLowerCase(Locale.ROOT);
        if (type.contains("nha")) {
            holder.imgAddressType.setImageResource(R.drawable.ic_home);
        } else if (type.contains("cong")) {
            holder.imgAddressType.setImageResource(R.drawable.ic_work);
        } else {
            holder.imgAddressType.setImageResource(R.drawable.ic_location_on);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
        holder.btnEditAddress.setOnClickListener(v -> {
            if (editListener != null) editListener.onAction(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnAddressClickListener {
        void onClick(DeliveryAddress item);
    }

    public interface OnAddressActionListener {
        void onAction(DeliveryAddress item);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAddressType;
        ImageView btnEditAddress;
        TextView tvAddressLabel;
        TextView tvDefaultTag;
        TextView tvAddressDetail;
        TextView tvUserInfo;

        VH(@NonNull View view) {
            super(view);
            imgAddressType = view.findViewById(R.id.imgAddressType);
            btnEditAddress = view.findViewById(R.id.btnEditAddress);
            tvAddressLabel = view.findViewById(R.id.tvAddressLabel);
            tvDefaultTag = view.findViewById(R.id.tvDefaultTag);
            tvAddressDetail = view.findViewById(R.id.tvAddressDetail);
            tvUserInfo = view.findViewById(R.id.tvUserInfo);
        }
    }
}
