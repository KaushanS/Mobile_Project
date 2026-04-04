package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Update these ids if your XML uses different names
        etName = findViewById(R.id.CreateAccountFullName);
        etEmail = findViewById(R.id.CreateAccountEmail);
        etPassword = findViewById(R.id.CreateAccountPassword);
        etConfirmPassword = findViewById(R.id.CreateAccountConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());


        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
            // keep your Google flow here later
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        checkEmailExists(email, name, password);
    }

    private void checkEmailExists(String email, String name, String password) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getSignInMethods() == null || task.getResult().getSignInMethods().isEmpty();

                        if (!isNewUser) {
                            etEmail.setError("This email is already registered");
                            etEmail.requestFocus();
                            Toast.makeText(this, "Email already exists. Please use a different email or login.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        createAccount(email, name, password);
                    } else {
                        Toast.makeText(this, "Error checking email. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAccount(String email, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> sendEmailVerificationAndOpenWaiting());
                    } else {
                        String message = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerificationAndOpenWaiting() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent. Please verify your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Could not send verification email now.", Toast.LENGTH_LONG).show();
                        }

                        startActivity(new Intent(this, EmailVerificationWaitingActivity.class));
                        finish();
                    });
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
}
