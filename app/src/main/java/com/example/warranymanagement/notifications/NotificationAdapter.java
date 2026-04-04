package com.example.warranymanagement.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private final List<AppNotification> items = new ArrayList<>();

    public void setItems(List<AppNotification> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppNotification n = items.get(position);
        h.tvTitle.setText(n.title == null || n.title.isEmpty() ? "Notification" : n.title);
        h.tvSubtitle.setText(n.message == null ? "" : n.message);
        h.tvTime.setText(n.createdAtText == null ? "" : n.createdAtText);

        h.accentBar.setBackgroundColor(n.read ? 0xFFBDBDBD : 0xFF1D8E66);

        if ("delete".equalsIgnoreCase(n.type)) {
            h.iconBg.setCardBackgroundColor(0xFFFFEBEB);
            h.icon.setImageResource(android.R.drawable.ic_dialog_alert);
            h.icon.setColorFilter(0xFFFF5252);
            h.accentBar.setBackgroundColor(0xFFFF5252);
        } else if ("edit".equalsIgnoreCase(n.type)) {
            h.iconBg.setCardBackgroundColor(0xFFFFF8E1);
            h.icon.setImageResource(android.R.drawable.ic_menu_edit);
            h.icon.setColorFilter(0xFFE6A23C);
        } else if ("password".equalsIgnoreCase(n.type)) {
            h.iconBg.setCardBackgroundColor(0xFFE8F5E9);
            h.icon.setImageResource(android.R.drawable.ic_lock_lock);
            h.icon.setColorFilter(0xFF1D8E66);
        } else {
            h.iconBg.setCardBackgroundColor(0xFFE8F5E9);
            h.icon.setImageResource(android.R.drawable.checkbox_on_background);
            h.icon.setColorFilter(0xFF1D8E66);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View accentBar;
        CardView iconBg;
        ImageView icon;
        TextView tvTitle, tvSubtitle, tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            accentBar = itemView.findViewById(R.id.vAccentBorder);
            iconBg = itemView.findViewById(R.id.cvNotifIconBg);
            icon = itemView.findViewById(R.id.ivNotifIcon);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvSubtitle = itemView.findViewById(R.id.tvNotifSubtitle);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
