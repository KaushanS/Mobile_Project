package com.example.warranymanagement.warranty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WarrantyListAdapter extends RecyclerView.Adapter<WarrantyListAdapter.WarrantyViewHolder> {

    public interface OnWarrantyClickListener {
        void onWarrantyClick(WarrantyItem item);
    }

    private final List<WarrantyItem> items = new ArrayList<>();
    private OnWarrantyClickListener onWarrantyClickListener;

    public void setOnWarrantyClickListener(OnWarrantyClickListener listener) {
        this.onWarrantyClickListener = listener;
    }

    public void submitList(List<WarrantyItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WarrantyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warranty_list, parent, false);
        return new WarrantyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WarrantyViewHolder holder, int position) {
        WarrantyItem item = items.get(position);

        holder.tvProductName.setText(safe(item.getProductName()));
        holder.tvExpiryDate.setText("Expires: " + safe(item.getExpiryDate()));

        String photoUrl = item.getPhotoUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            loadImage(holder.ivWarrantyPhoto, photoUrl.trim());
        } else {
            holder.ivWarrantyPhoto.setImageResource(android.R.color.darker_gray);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onWarrantyClickListener != null) {
                onWarrantyClickListener.onWarrantyClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void loadImage(ImageView imageView, String urlString) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                try (InputStream input = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    imageView.post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                imageView.post(() -> imageView.setImageResource(android.R.color.darker_gray));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    static class WarrantyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWarrantyPhoto;
        TextView tvProductName;
        TextView tvExpiryDate;

        WarrantyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWarrantyPhoto = itemView.findViewById(R.id.ivWarrantyPhoto);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
        }
    }
}