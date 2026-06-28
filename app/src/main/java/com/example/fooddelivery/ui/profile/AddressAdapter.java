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

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

    private final Context context;
    private final List<DeliveryAddress> items = new ArrayList<>();

    public interface OnAddressClickListener {
        void onClick(DeliveryAddress item);
    }

    private OnAddressClickListener listener;

    public AddressAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DeliveryAddress> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DeliveryAddress item = items.get(position);
        holder.tvAddressLabel.setText(item.getLabel());
        holder.tvAddressDetail.setText(item.getFullAddress());
        holder.tvUserInfo.setText(item.toCheckoutDisplayText().replace('\n', ' '));
        holder.tvDefaultTag.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);

        String label = item.getLabel() == null ? "" : item.getLabel().toLowerCase();
        if (label.contains("nh")) {
            holder.imgAddressType.setImageResource(R.drawable.ic_home);
        } else if (label.contains("ty")) {
            holder.imgAddressType.setImageResource(R.drawable.ic_work);
        } else {
            holder.imgAddressType.setImageResource(R.drawable.ic_location_on);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAddressType;
        TextView tvAddressLabel, tvDefaultTag, tvAddressDetail, tvUserInfo;

        VH(@NonNull View v) {
            super(v);
            imgAddressType = v.findViewById(R.id.imgAddressType);
            tvAddressLabel = v.findViewById(R.id.tvAddressLabel);
            tvDefaultTag = v.findViewById(R.id.tvDefaultTag);
            tvAddressDetail = v.findViewById(R.id.tvAddressDetail);
            tvUserInfo = v.findViewById(R.id.tvUserInfo);
        }
    }
}
