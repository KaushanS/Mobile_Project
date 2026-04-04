package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etForgotEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        etForgotEmail = findViewById(R.id.etForgotEmail);

        findViewById(R.id.btnResetPassword).setOnClickListener(v -> sendResetLink());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void sendResetLink() {
        String email = etForgotEmail.getText().toString().trim();

        etForgotEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            etForgotEmail.setError("Email is required");
            etForgotEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etForgotEmail.setError("Please enter a valid email address");
            etForgotEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ResetEmailSentActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send reset email. Please check the Gmail and try again.", Toast.LENGTH_LONG).show());
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
}