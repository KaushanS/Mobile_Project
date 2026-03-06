package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.auth.LoginActivity;
import com.example.mobileproject.auth.RegisterActivity;

public class MainActivity_Splash extends AppCompatActivity {

    Button btnGetStarted, btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_splash);

        btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity_Splash.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin = findViewById(R.id.btn_already_have_account);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity_Splash.this, LoginActivity.class);
            startActivity(intent);
        });

    }
}