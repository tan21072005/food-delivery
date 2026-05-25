package com.example.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.model.SettingItem;

import java.util.List;

public class SettingAdapter
        extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private Context context;
    private List<SettingItem> list;

    public SettingAdapter(Context context,
                          List<SettingItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_menu_row,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        SettingItem item = list.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.imgIcon.setImageResource(item.getIcon());

        holder.cardItem.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context,
                    item.getActivityClass()
            );
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgIcon, imgArrow;
        TextView txtTitle;
        CardView cardItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            cardItem = itemView.findViewById(R.id.cardItem);
        }
    }
}