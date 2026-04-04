package com.example.warranymanagement.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().trim().isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String email = currentUser.getEmail().trim();

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    NotificationPublisher.publishToUser(
                            currentUser.getUid(),
                            "password",
                            "Password Reset Link Sent",
                            "Password reset link sent to " + email
                    );
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show());
    }
}
