package com.example.warranymanagement.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.warranymanagement.R;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.notifications.NotificationsActivity;
import com.example.warranymanagement.settings.SettingsActivity;
import com.example.warranymanagement.warranty.AddWarrantyActivity;
import com.example.warranymanagement.warranty.WarrantyDetailActivity;
import com.example.warranymanagement.warranty.WarrantyListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int EXPIRING_SOON_DAYS = 30;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvUserName;
    private TextView tvTotalCount;
    private TextView tvExpiringCount;
    private TextView tvExpiredCount;
    private LinearLayout llExpiringSoonContainer;
    private TextView tvExpiringSoonDetails;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final List<String[]> expiringSoonItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.UserName);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvExpiringCount = findViewById(R.id.tvExpiringCount);
        tvExpiredCount = findViewById(R.id.tvExpiredCount);
        llExpiringSoonContainer = findViewById(R.id.llExpiringSoonContainer);
        tvExpiringSoonDetails = findViewById(R.id.tvExpiringSoonDetails);

        findViewById(R.id.AddWarranty).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWarrantyActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.Notification).setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_home);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_mine) {
                startActivity(new Intent(this, WarrantyListActivity.class));
                return true;
            } else if (itemId == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        loadUserName();
        loadWarrantyDashboardData();
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            tvUserName.setText("Guest");
            return;
        }

        // First priority: Firebase Auth display name
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            tvUserName.setText(user.getDisplayName().trim());
            return;
        }

        // Fallback: Firestore users collection (users/{uid}.name)
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    if (name != null && !name.trim().isEmpty()) {
                        tvUserName.setText(name.trim());
                    } else {
                        tvUserName.setText(user.getEmail() != null ? user.getEmail() : "User");
                    }
                })
                .addOnFailureListener(e ->
                        tvUserName.setText(user.getEmail() != null ? user.getEmail() : "User"));
    }

    private void loadWarrantyDashboardData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;


        db.collection("users")
                .document(user.getUid())
                .collection("warranties")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = 0;
                    int expiring = 0;
                    int expired = 0;
                    expiringSoonItems.clear();

                    Date today = clearTime(new Date());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.add(Calendar.DAY_OF_YEAR, EXPIRING_SOON_DAYS);
                    Date soonLimit = cal.getTime();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        total++;

                        String warrantyId = doc.getId();
                        String expiryDateString = doc.getString("expiryDate");
                        String productName = doc.getString("productName");
                        String storeName = doc.getString("storeName");
                        String category = doc.getString("category");

                        if (productName == null || productName.trim().isEmpty()) productName = "Warranty Item";
                        if (storeName == null) storeName = "";
                        if (category == null) category = "";

                        Date expiryDate = parseDate(expiryDateString);
                        if (expiryDate == null) continue;

                        if (expiryDate.before(today)) {
                            expired++;
                        } else if (!expiryDate.after(soonLimit)) {
                            expiring++;
                            expiringSoonItems.add(new String[]{warrantyId, productName, storeName, category, expiryDateString});
                        }
                    }

                    tvTotalCount.setText(String.valueOf(total));
                    tvExpiringCount.setText(String.valueOf(expiring));
                    tvExpiredCount.setText(String.valueOf(expired));

                    renderExpiringSoon(expiringSoonItems);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load dashboard data", Toast.LENGTH_SHORT).show());
    }

    private void renderExpiringSoon(List<String[]> items) {
        if (llExpiringSoonContainer == null || tvExpiringSoonDetails == null) return;

        llExpiringSoonContainer.removeAllViews();

        if (items.isEmpty()) {
            tvExpiringSoonDetails.setVisibility(View.VISIBLE);
            tvExpiringSoonDetails.setText("No warranties expiring soon.");
            llExpiringSoonContainer.addView(tvExpiringSoonDetails);
            return;
        }

        tvExpiringSoonDetails.setVisibility(View.GONE);

        for (String[] item : items) {
            String warrantyId = item[0];
            String product = item[1];
            String store = item[2];
            String category = item[3];
            String expiry = item[4];

            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dpToPx(12));
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            card.setRadius(dpToPx(18));
            card.setCardElevation(dpToPx(2));
            card.setClickable(true);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, WarrantyDetailActivity.class);
                intent.putExtra(WarrantyDetailActivity.EXTRA_WARRANTY_ID, warrantyId);
                startActivity(intent);
            });

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView badge = new TextView(this);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dpToPx(42), dpToPx(42));
            badge.setLayoutParams(badgeParams);
            badge.setText("⏳");
            badge.setTextSize(20f);
            badge.setGravity(android.view.Gravity.CENTER);
            badge.setBackgroundColor(0xFFFFF4CC);
            badge.setTextColor(0xFFB26A00);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            infoParams.setMargins(dpToPx(12), 0, 0, 0);
            info.setLayoutParams(infoParams);

            TextView title = new TextView(this);
            title.setText(product);
            title.setTextColor(getResources().getColor(R.color.black, getTheme()));
            title.setTextSize(16f);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

            TextView subtitle = new TextView(this);
            StringBuilder sb = new StringBuilder();
            if (!store.trim().isEmpty()) sb.append(store).append(" • ");
            if (!category.trim().isEmpty()) sb.append(category).append(" • ");
            sb.append("Expires: ").append(formatExpiry(expiry));
            subtitle.setText(sb.toString());
            subtitle.setTextColor(0xFF666666);
            subtitle.setTextSize(13f);
            subtitle.setPadding(0, dpToPx(4), 0, 0);

            info.addView(title);
            info.addView(subtitle);
            row.addView(badge);
            row.addView(info);
            card.addView(row);
            llExpiringSoonContainer.addView(card);
        }
    }

    private String formatExpiry(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return "-";
        Date d = parseDate(isoDate);
        if (d == null) return isoDate;
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(d);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return clearTime(dateFormat.parse(dateStr.trim()));
        } catch (ParseException e) {
            return null;
        }
    }

    private Date clearTime(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}