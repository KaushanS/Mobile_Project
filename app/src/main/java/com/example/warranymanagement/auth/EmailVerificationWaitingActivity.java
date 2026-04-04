package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationWaitingActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseAuth auth;
    private boolean active = false;

    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!active) return;
            checkVerificationStatus();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_waiting);

        auth = FirebaseAuth.getInstance();

        TextView btnResend = findViewById(R.id.btnResendVerification);

        btnResend.setOnClickListener(v -> resendVerificationEmail());

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        handler.post(checkRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        handler.removeCallbacks(checkRunnable);
    }

    private void checkVerificationStatus() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return;

        current.reload()
                .addOnSuccessListener(unused -> {
                    FirebaseUser refreshed = auth.getCurrentUser();
                    if (refreshed != null && refreshed.isEmailVerified()) {
                        Toast.makeText(this, "Email verified. Please login.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return;

        current.sendEmailVerification()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Verification email resent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to resend email", Toast.LENGTH_SHORT).show());
    }

    public void onBackToLogin(View view) {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

