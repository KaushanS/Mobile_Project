package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etName, etEmail, etPassword, etConfirmPassword, etNic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.CreateAccountFullName);
        etEmail = findViewById(R.id.CreateAccountEmail);
        etPassword = findViewById(R.id.CreateAccountPassword);
        etConfirmPassword = findViewById(R.id.CreateAccountConfirmPassword);
        etNic = findViewById(R.id.CreateAccountNic);

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String nic = etNic.getText().toString().trim().toUpperCase(Locale.US);

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

        if (TextUtils.isEmpty(nic)) {
            etNic.setError("NIC is required");
            etNic.requestFocus();
            return;
        }

        if (!isValidSriLankanNic(nic)) {
            etNic.setError("Enter a valid nic number");
            etNic.requestFocus();
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

        checkEmailExists(email, name, nic, password);
    }

    private void checkEmailExists(String email, String name, String nic, String password) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        String message = e != null ? e.getMessage() : "Failed to check email";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SignInMethodQueryResult result = task.getResult();
                    boolean isNewUser = result == null || result.getSignInMethods() == null || result.getSignInMethods().isEmpty();

                    if (!isNewUser) {
                        etEmail.setError("This email is already registered");
                        etEmail.requestFocus();
                        Toast.makeText(this, "Email already exists. Please use a different email or login.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createAccount(email, name, nic, password);
                });
    }

    private void createAccount(String email, String name, String nic, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();

                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    saveUserProfile(uid, name, email, nic);
                                    sendEmailVerificationAndOpenWaiting();
                                });
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile(String uid, String name, String email, String nic) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("nic", nic);

        db.collection("users").document(uid).set(data);
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

    private boolean isValidSriLankanNic(String nic) {
        return nic.matches("^[0-9]{9}[VvXx]$") || nic.matches("^[0-9]{12}$");
    }
}
