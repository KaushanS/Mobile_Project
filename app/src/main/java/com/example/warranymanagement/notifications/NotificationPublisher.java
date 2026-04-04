package com.example.warranymanagement.notifications;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class NotificationPublisher {

    private NotificationPublisher() {}

    public static void publishToUser(String uid, String type, String title, String message) {
        if (uid == null || uid.trim().isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("type", type == null ? "general" : type);
        data.put("title", title == null ? "Notification" : title);
        data.put("message", message == null ? "" : message);
        data.put("createdAt", Timestamp.now());
        data.put("read", false);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("notifications")
                .add(data);
    }
}

