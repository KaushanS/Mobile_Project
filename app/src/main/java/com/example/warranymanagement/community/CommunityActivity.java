package com.example.warranymanagement.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.example.warranymanagement.settings.SettingsActivity;
import com.example.warranymanagement.warranty.AddWarrantyActivity;
import com.example.warranymanagement.warranty.WarrantyListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommunityActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ReviewAdapter adapter;
    private final List<Map<String, Object>> allReviews = new ArrayList<>();
    private String selectedCategory = "All";

    private TextView chipAllText;
    private TextView chipElectronicsText;
    private TextView chipAppliancesText;
    private TextView chipVehicleText;
    private android.view.View chipAll;
    private android.view.View chipElectronics;
    private android.view.View chipAppliances;
    private android.view.View chipVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        db = FirebaseFirestore.getInstance();

        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        adapter = new ReviewAdapter();
        adapter.setCurrentUserId(FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "");
        adapter.setReviewClickListener(this::showReviewActions);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(adapter);

        findViewById(R.id.fabWriteReview).setOnClickListener(v -> startActivity(new Intent(this, WriteReviewActivity.class)));
        findViewById(R.id.AddWarranty).setOnClickListener(v -> startActivity(new Intent(this, AddWarrantyActivity.class)));

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_community);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_mine) {
                startActivity(new Intent(this, WarrantyListActivity.class));
                return true;
            } else if (itemId == R.id.nav_community) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        chipAll = findViewById(R.id.chipAll);
        chipElectronics = findViewById(R.id.chipElectronics);
        chipAppliances = findViewById(R.id.chipAppliances);
        chipVehicle = findViewById(R.id.chipVehicle);

        chipAllText = ((TextView) ((android.view.ViewGroup) chipAll).getChildAt(0));
        chipElectronicsText = findChipText(chipElectronics);
        chipAppliancesText = findChipText(chipAppliances);
        chipVehicleText = findChipText(chipVehicle);

        chipAll.setOnClickListener(v -> setSelectedCategory("All"));
        chipElectronics.setOnClickListener(v -> setSelectedCategory("Electronics"));
        chipAppliances.setOnClickListener(v -> setSelectedCategory("Appliances"));
        chipVehicle.setOnClickListener(v -> setSelectedCategory("Vehicle"));

        updateChipStyles();
        loadReviews();
    }

    private TextView findChipText(android.view.View chip) {
        if (chip instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) chip;
            for (int i = 0; i < group.getChildCount(); i++) {
                android.view.View child = group.getChildAt(i);
                if (child instanceof TextView) return (TextView) child;
            }
        }
        return null;
    }

    private void setSelectedCategory(String category) {
        selectedCategory = category;
        updateChipStyles();
        filterAndRender();
    }

    private void updateChipStyles() {
        styleChip(chipAll, chipAllText, "All".equalsIgnoreCase(selectedCategory));
        styleChip(chipElectronics, chipElectronicsText, "Electronics".equalsIgnoreCase(selectedCategory));
        styleChip(chipAppliances, chipAppliancesText, "Appliances".equalsIgnoreCase(selectedCategory));
        styleChip(chipVehicle, chipVehicleText, "Vehicle".equalsIgnoreCase(selectedCategory));
    }

    private void styleChip(android.view.View chip, TextView text, boolean selected) {
        if (chip == null || text == null) return;
        int bgColor = selected ? 0xFF1D664D : 0xFFFFFFFF;
        int textColor = selected ? 0xFFFFFFFF : 0xFF000000;
        int strokeColor = selected ? 0xFF1D664D : 0xFFE0E0E0;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(60f);
        drawable.setColor(bgColor);
        drawable.setStroke(2, strokeColor);
        chip.setBackground(drawable);
        text.setTextColor(textColor);
    }

    private void loadReviews() {
        db.collection("community_reviews")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allReviews.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            java.util.Map<String, Object> item = new java.util.HashMap<>(doc.getData());
                            item.put("id", doc.getId());
                            allReviews.add(item);
                        }
                    }
                    filterAndRender();
                });
    }

    private void filterAndRender() {
        if (selectedCategory == null || selectedCategory.equalsIgnoreCase("All")) {
            adapter.setItems(new ArrayList<>(allReviews));
            return;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : allReviews) {
            String category = value(item.get("category"));
            if (selectedCategory.equalsIgnoreCase(category)) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    private void showReviewActions(String reviewId, boolean isMine) {
        if (!isMine || reviewId == null || reviewId.trim().isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Your Review")
                .setMessage("What do you want to do with this review?")
                .setPositiveButton("Update", (d, w) -> {
                    Intent intent = new Intent(this, WriteReviewActivity.class);
                    intent.putExtra("reviewId", reviewId);
                    startActivity(intent);
                })
                .setNegativeButton("Delete", (d, w) -> confirmDeleteReview(reviewId))
                .setNeutralButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void confirmDeleteReview(String reviewId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Yes", (dialog, which) -> db.collection("community_reviews").document(reviewId).delete()
                        .addOnSuccessListener(unused -> {
                            String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    : "";
                            NotificationPublisher.publishToUser(
                                    uid,
                                    "delete",
                                    "Review Deleted",
                                    "Your review was deleted"
                            );
                            Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String value(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }
}