package com.example.fooddelivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER    = 0;
    private static final int TYPE_SECTION   = 1;
    private static final int TYPE_MENU_ITEM = 2;

    private final List<ProfileMenuItem> items;
    private final OnItemClickListener   listener;

    public interface OnItemClickListener {
        void onMenuItemClick(String itemId);
        void onSwitchAccountClick();
        void onEditProfileClick();
        void onMoreClick();
    }

    public ProfileMenuAdapter(List<ProfileMenuItem> items, OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).getType()) {
            case HEADER:    return TYPE_HEADER;
            case SECTION:   return TYPE_SECTION;
            case MENU_ITEM: return TYPE_MENU_ITEM;
            default:        return TYPE_MENU_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inf.inflate(R.layout.fragment_item_header_profile, parent, false));
            case TYPE_SECTION:
                return new SectionViewHolder(inf.inflate(R.layout.fragment_item_section_profile, parent, false));
            default:
                return new MenuItemViewHolder(inf.inflate(R.layout.fragment_item_menu_profile, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileMenuItem item = items.get(position);
        switch (item.getType()) {
            case HEADER:
                ((HeaderViewHolder) holder).bind(listener);
                break;
            case SECTION:
                ((SectionViewHolder) holder).bind(item);
                break;
            case MENU_ITEM:
                ((MenuItemViewHolder) holder).bind(item, listener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ── ViewHolder: HEADER ──
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView btnMore;
        private final ImageView btnSwitchAccount;
        private final ImageView btnEditProfile;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            btnMore          = itemView.findViewById(R.id.btnMore);
            btnSwitchAccount = itemView.findViewById(R.id.btnSwitchAccount);
            btnEditProfile   = itemView.findViewById(R.id.btnEditProfile);
        }

        void bind(OnItemClickListener listener) {
            if (listener == null) return;
            btnMore.setOnClickListener(v          -> listener.onMoreClick());
            btnSwitchAccount.setOnClickListener(v -> listener.onSwitchAccountClick());
            btnEditProfile.setOnClickListener(v   -> listener.onEditProfileClick());
        }
    }

    // ── ViewHolder: SECTION ──
    static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionTitle;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }

        void bind(ProfileMenuItem item) {
            tvSectionTitle.setText(item.getSectionTitle());
        }
    }

    // ── ViewHolder: MENU_ITEM ──
    static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rowContainer;
        private final ImageView    ivIcon;
        private final TextView     tvLabel;
        private final ImageView    ivArrow;
        private final View         divider;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainer);
            ivIcon       = itemView.findViewById(R.id.ivIcon);
            tvLabel      = itemView.findViewById(R.id.tvLabel);
            ivArrow      = itemView.findViewById(R.id.ivArrow);
            divider      = itemView.findViewById(R.id.divider);
        }

        void bind(ProfileMenuItem item, OnItemClickListener listener) {
            ivIcon.setImageResource(item.getIconRes());
            tvLabel.setText(item.getLabel());
            ivArrow.setVisibility(item.isShowArrow() ? View.VISIBLE : View.GONE);
            divider.setVisibility(item.isShowDivider() ? View.VISIBLE : View.GONE);
            rowContainer.setOnClickListener(v -> {
                if (listener != null) listener.onMenuItemClick(item.getItemId());
            });
        }
    }
}