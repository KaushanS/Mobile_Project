package com.example.warranymanagement.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.settings.SettingsActivity;
import com.example.warranymanagement.warranty.AddWarrantyActivity;
import com.example.warranymanagement.warranty.WarrantyListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private NotificationAdapter adapter;
    private final List<AppNotification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView rv = findViewById(R.id.rvNotifications);
        adapter = new NotificationAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TextView btnMarkRead = findViewById(R.id.btnMarkRead);
        btnMarkRead.setOnClickListener(v -> markAllAsRead());

        findViewById(R.id.AddWarranty).setOnClickListener(v ->
                startActivity(new Intent(this, AddWarrantyActivity.class)));

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_home);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
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

        loadNotifications();
    }

    private void loadNotifications() {
        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("notifications")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notifications.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AppNotification n = new AppNotification();
                            n.id = doc.getId();
                            n.type = safe(doc.getString("type"));
                            n.title = safe(doc.getString("title"));
                            n.message = safe(doc.getString("message"));
                            n.read = Boolean.TRUE.equals(doc.getBoolean("read"));

                            Object ts = doc.get("createdAt");
                            if (ts instanceof com.google.firebase.Timestamp) {
                                Date d = ((com.google.firebase.Timestamp) ts).toDate();
                                n.createdAtText = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(d);
                            } else {
                                n.createdAtText = "";
                            }

                            notifications.add(n);
                        }
                    }
                    adapter.setItems(notifications);
                });
    }

    private void markAllAsRead() {
        if (user == null) return;
        for (AppNotification n : notifications) {
            if (!n.read && n.id != null && !n.id.isEmpty()) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("notifications")
                        .document(n.id)
                        .update("read", true);
            }
        }
        Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}