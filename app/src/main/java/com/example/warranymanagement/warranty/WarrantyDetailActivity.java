package com.example.warranymanagement.warranty;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WarrantyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_WARRANTY_ID = "warrantyId";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String warrantyId;
    private String currentPhotoUrl = "";

    private TextView tvDetailProductName;
    private TextView tvDetailStoreNameHeader;
    private TextView tvExpiryDateBig;
    private TextView tvDaysLeftBig;
    private ImageView ivWarrantyCardDetail;
    private TextView tvDetailPurchaseDate;
    private TextView tvDetailDuration;
    private TextView tvDetailCategory;
    private TextView tvDetailStoreName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_detail);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        warrantyId = getIntent().getStringExtra(EXTRA_WARRANTY_ID);

        tvDetailProductName = findViewById(R.id.tvDetailProductName);
        tvDetailStoreNameHeader = findViewById(R.id.tvDetailStoreNameHeader);
        tvExpiryDateBig = findViewById(R.id.tvExpiryDateBig);
        tvDaysLeftBig = findViewById(R.id.tvDaysLeftBig);
        ivWarrantyCardDetail = findViewById(R.id.ivWarrantyCardDetail);
        tvDetailPurchaseDate = findViewById(R.id.tvDetailPurchaseDate);
        tvDetailDuration = findViewById(R.id.tvDetailDuration);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailStoreName = findViewById(R.id.tvDetailStoreName);

        ivWarrantyCardDetail.setOnClickListener(v -> {
            if (currentPhotoUrl == null || currentPhotoUrl.isEmpty()) {
                Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ImageFullScreenActivity.class);
            intent.putExtra(ImageFullScreenActivity.EXTRA_IMAGE_URL, currentPhotoUrl);
            startActivity(intent);
        });

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditWarrantyActivity.class);
            intent.putExtra(EXTRA_WARRANTY_ID, warrantyId);
            startActivity(intent);
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmation());

        loadWarrantyDetails();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Warranty")
                .setMessage("Are you sure you want to delete this warranty?")
                .setPositiveButton("Yes", (dialog, which) -> deleteWarranty())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadWarrantyDetails() {
        if (currentUser == null || warrantyId == null || warrantyId.trim().isEmpty()) {
            Toast.makeText(this, "Warranty not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("warranties")
                .document(warrantyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Warranty not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String productName = safe(doc.getString("productName"));
                    String storeName = safe(doc.getString("storeName"));
                    String purchaseDate = safe(doc.getString("purchaseDate"));
                    String warrantyPeriod = safe(doc.getString("warrantyPeriod"));
                    String category = safe(doc.getString("category"));
                    String expiryDate = safe(doc.getString("expiryDate"));
                    String photoUrl = safe(doc.getString("photoUrl"));

                    tvDetailProductName.setText(productName);
                    tvDetailStoreNameHeader.setText(storeName);
                    tvExpiryDateBig.setText(formatBigDate(expiryDate));
                    tvDaysLeftBig.setText(String.valueOf(calculateDaysLeft(expiryDate)));

                    tvDetailPurchaseDate.setText(formatShortDate(purchaseDate));
                    tvDetailDuration.setText(warrantyPeriod.isEmpty() ? "-" : warrantyPeriod);
                    tvDetailCategory.setText(category.isEmpty() ? "-" : category);

                    tvDetailStoreName.setText(storeName.isEmpty() ? "-" : storeName);

                    currentPhotoUrl = photoUrl;

                    if (!photoUrl.isEmpty()) {
                        loadImageFromUrl(photoUrl, ivWarrantyCardDetail);
                    } else {
                        ivWarrantyCardDetail.setImageResource(android.R.color.darker_gray);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show());
    }

    private void deleteWarranty() {
        if (currentUser == null || warrantyId == null || warrantyId.trim().isEmpty()) {
            finish();
            return;
        }

        String productName = tvDetailProductName != null ? tvDetailProductName.getText().toString().trim() : "Warranty";

        db.collection("users")
                .document(currentUser.getUid())
                .collection("warranties")
                .document(warrantyId)
                .delete()
                .addOnSuccessListener(unused -> {
                    NotificationPublisher.publishToUser(
                            currentUser.getUid(),
                            "delete",
                            "Warranty Deleted",
                            productName + " warranty deleted"
                    );
                    Toast.makeText(this, "Warranty deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show());
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String formatBigDate(String isoDate) {
        if (isoDate.isEmpty()) return "-";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = in.parse(isoDate);
            if (d == null) return isoDate;
            SimpleDateFormat out = new SimpleDateFormat("MMM dd,\nyyyy", Locale.US);
            return out.format(d);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private String formatShortDate(String isoDate) {
        if (isoDate.isEmpty()) return "-";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = in.parse(isoDate);
            if (d == null) return isoDate;
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            return out.format(d);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private int calculateDaysLeft(String isoDate) {
        if (isoDate.isEmpty()) return 0;
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date expiry = in.parse(isoDate);
            if (expiry == null) return 0;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffMs = expiry.getTime() - today.getTimeInMillis();
            return (int) (diffMs / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            return 0;
        }
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.setRequestProperty("Accept", "image/*");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.connect();

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Image load failed: HTTP " + code, Toast.LENGTH_SHORT).show();
                        imageView.setImageResource(android.R.color.darker_gray);
                    });
                    return;
                }

                input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null) {
                    runOnUiThread(() -> {
                        imageView.setImageBitmap(bitmap);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                        imageView.setImageResource(android.R.color.darker_gray);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    imageView.setImageResource(android.R.color.darker_gray);
                });
            } finally {
                try {
                    if (input != null) input.close();
                } catch (Exception ignored) {}
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}