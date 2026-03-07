package com.example.warranymanagement.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        findViewById(R.id.btnResetPassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetEmailSentActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }
}