package com.example.warranymanagement.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.auth.LoginActivity;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.profile.ChangePasswordActivity;
import com.example.warranymanagement.profile.EditProfileActivity;
import com.example.warranymanagement.warranty.AddWarrantyActivity;
import com.example.warranymanagement.warranty.WarrantyListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        loadUserData();

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btnChangePassword).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        findViewById(R.id.btnLogoutCard).setOnClickListener(v -> confirmLogout());

        findViewById(R.id.AddWarranty).setOnClickListener(v ->
                startActivity(new Intent(this, AddWarrantyActivity.class)));

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_settings);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_mine) {
                startActivity(new Intent(this, WarrantyListActivity.class));
                return true;
            } else if (itemId == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            TextView tvName = findViewById(R.id.tvSettingsName);
            TextView tvEmail = findViewById(R.id.tvSettingsEmail);

            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            tvName.setText(displayName != null && !displayName.isEmpty() ? displayName : "User");
            tvEmail.setText(email != null ? email : "No email");
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}