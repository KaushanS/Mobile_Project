package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.LoginEmail);
        etPassword = findViewById(R.id.LoginPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginUser());

        findViewById(R.id.RegisterLink).setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.ForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        etEmail.setError(null);
        etPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Intent intent = new Intent(this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            mAuth.signOut();
                            etEmail.setError("Please verify your Gmail first");
                            etEmail.requestFocus();
                            Toast.makeText(this, "Please verify your Gmail before login.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Exception ex = task.getException();

                        if (ex instanceof FirebaseAuthInvalidUserException) {

                            etEmail.setError("Gmail is incorrect");
                            etEmail.requestFocus();
                        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                            String code = ((FirebaseAuthInvalidCredentialsException) ex).getErrorCode();

                            if ("ERROR_INVALID_EMAIL".equals(code)) {
                                etEmail.setError("Gmail is incorrect");
                                etEmail.requestFocus();
                            } else if ("ERROR_WRONG_PASSWORD".equals(code)) {
                                etPassword.setError("Password is incorrect");
                                etPassword.requestFocus();
                            } else {
                                etPassword.setError("Password is incorrect");
                                etPassword.requestFocus();
                            }
                        }
                    }

                });
    }

}
