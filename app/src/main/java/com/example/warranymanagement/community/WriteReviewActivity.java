package com.example.warranymanagement.community;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WriteReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String reviewId;

    private EditText etReviewProductName;
    private EditText etReviewStoreName;
    private EditText etReviewText;
    private AutoCompleteTextView actCategory;
    private RatingBar ratingBar;

    private String selectedCategory = "Electronics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reviewId = getIntent().getStringExtra("reviewId");

        etReviewProductName = findViewById(R.id.etReviewProductName);
        etReviewStoreName = findViewById(R.id.etReviewStoreName);
        etReviewText = findViewById(R.id.etReviewText);
        actCategory = findViewById(R.id.actCategory);
        ratingBar = findViewById(R.id.ratingBar);
        if (ratingBar != null) {
            ratingBar.setIsIndicator(false);
        }

        setupCategoryDropdown();
        findViewById(R.id.btnPostReview).setOnClickListener(v -> publishOrUpdateReview());

        if (!TextUtils.isEmpty(reviewId)) {
            loadExistingReview();
        }
    }

    private void setupCategoryDropdown() {
        if (actCategory == null) return;

        String[] categories = new String[]{"Electronics", "Appliances", "Vehicle"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(adapter);
        actCategory.setKeyListener(null);
        actCategory.setFocusable(false);
        actCategory.setClickable(true);
        actCategory.setOnClickListener(v -> actCategory.showDropDown());
        actCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories[position];
            actCategory.setText(selectedCategory, false);
        });
    }

    private void loadExistingReview() {
        if (currentUser == null) return;
        db.collection("community_reviews").document(reviewId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    if (currentUser.getUid().equals(String.valueOf(doc.getString("userId")))) {
                        etReviewProductName.setText(doc.getString("productName"));
                        etReviewStoreName.setText(doc.getString("storeName"));
                        etReviewText.setText(doc.getString("reviewText"));
                        selectedCategory = doc.getString("category") != null ? doc.getString("category") : selectedCategory;
                        actCategory.setText(selectedCategory, false);

                        Double rating = doc.getDouble("rating");
                        if (rating != null) {
                            ratingBar.setRating(rating.floatValue());
                        }

                        ((android.widget.Button) findViewById(R.id.btnPostReview)).setText(R.string.btn_update_review);
                    } else {
                        Toast.makeText(this, "You can only edit your own review", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void publishOrUpdateReview() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String productName = etReviewProductName == null ? "" : etReviewProductName.getText().toString().trim();
        String storeName = etReviewStoreName == null ? "" : etReviewStoreName.getText().toString().trim();
        String reviewText = etReviewText == null ? "" : etReviewText.getText().toString().trim();
        float rating = ratingBar != null ? ratingBar.getRating() : 0;

        if (TextUtils.isEmpty(productName)) {
            etReviewProductName.setError("Product name is required");
            etReviewProductName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(reviewText)) {
            etReviewText.setError("Review text is required");
            etReviewText.requestFocus();
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("userId", currentUser.getUid());
        review.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
        review.put("productName", productName);
        review.put("storeName", storeName);
        review.put("category", selectedCategory);
        review.put("reviewText", reviewText);
        review.put("rating", rating);
        review.put("createdAt", FieldValue.serverTimestamp());

        if (!TextUtils.isEmpty(reviewId)) {
            db.collection("community_reviews").document(reviewId)
                    .update(review)
                    .addOnSuccessListener(unused -> {
                        NotificationPublisher.publishToUser(
                                currentUser.getUid(),
                                "edit",
                                "Review Updated",
                                "Your review for " + productName + " was updated"
                        );
                        Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("community_reviews")
                    .add(review)
                    .addOnSuccessListener(doc -> {
                        NotificationPublisher.publishToUser(
                                currentUser.getUid(),
                                "add",
                                "Review Published",
                                "Your review for " + productName + " is now live"
                        );
                        Toast.makeText(this, "Review published successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to publish review", Toast.LENGTH_SHORT).show());
        }
    }
}