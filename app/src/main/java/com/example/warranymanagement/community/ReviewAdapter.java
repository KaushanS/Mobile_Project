package com.example.warranymanagement.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface ReviewClickListener {
        void onReviewClicked(String reviewId, boolean isMine);
    }

    private final List<Map<String, Object>> items = new ArrayList<>();
    private String currentUserId = "";
    private ReviewClickListener clickListener;

    public void setCurrentUserId(String uid) {
        currentUserId = uid == null ? "" : uid;
        notifyDataSetChanged();
    }

    public void setReviewClickListener(ReviewClickListener listener) {
        clickListener = listener;
    }

    public void setItems(List<Map<String, Object>> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        String reviewId = value(item.get("id"));
        String userId = value(item.get("userId"));
        String userName = value(item.get("userName"));
        String productName = value(item.get("productName"));
        String storeName = value(item.get("storeName"));
        String category = value(item.get("category"));
        String reviewText = value(item.get("reviewText"));
        String createdAt = formatDate(item.get("createdAt"));

        holder.tvUserName.setText(userName.isEmpty() ? "User" : userName);
        holder.tvReviewedProduct.setText(productName.isEmpty() ? "Product" : productName);
        holder.tvReviewText.setText(reviewText.isEmpty() ? "No review text." : reviewText);
        holder.tvMeta.setText((storeName.isEmpty() ? "" : storeName + " • ") + (category.isEmpty() ? "" : category));
        holder.tvReviewDate.setText(createdAt.isEmpty() ? "Posted recently" : "Posted on " + createdAt);

        boolean isMine = !currentUserId.isEmpty() && currentUserId.equals(userId);
        holder.tvPostedByYou.setVisibility(isMine ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && isMine) {
                clickListener.onReviewClicked(reviewId, true);
            }
        });

        String initial = userName.isEmpty() ? "U" : userName.substring(0, 1).toUpperCase(Locale.US);
        holder.tvAvatarInitial.setText(initial);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitial;
        TextView tvUserName;
        TextView tvReviewedProduct;
        TextView tvMeta;
        TextView tvReviewDate;
        TextView tvPostedByYou;
        TextView tvReviewText;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReviewedProduct = itemView.findViewById(R.id.tvReviewedProduct);
            tvMeta = itemView.findViewById(R.id.tvReviewMeta);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvPostedByYou = itemView.findViewById(R.id.tvPostedByYou);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
        }
    }

    private String value(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private String formatDate(Object createdAt) {
        if (createdAt == null) return "";
        if (createdAt instanceof com.google.firebase.Timestamp) {
            Date date = ((com.google.firebase.Timestamp) createdAt).toDate();
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }
        return "";
    }
}