package com.example.warranymanagement.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;
import com.example.warranymanagement.auth.LoginActivity;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.warranty.AddWarrantyActivity;
import com.example.warranymanagement.warranty.WarrantyListActivity;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.profile.EditProfileActivity;
import com.example.warranymanagement.profile.ChangePasswordActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Edit profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Logout
        findViewById(R.id.btnLogoutCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigation
        findViewById(R.id.AddWarranty).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWarrantyActivity.class);
            startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_settings);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;

            } else if (itemId == R.id.nav_mine) {
                Intent intent = new Intent(this, WarrantyListActivity.class);
                startActivity(intent);
                return true;

            } else if (itemId == R.id.nav_community) {
                Intent intent = new Intent(this, CommunityActivity.class);
                startActivity(intent);
                return true;

            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}