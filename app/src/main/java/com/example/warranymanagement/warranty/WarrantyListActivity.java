package com.example.warranymanagement.warranty;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.settings.SettingsActivity;
import com.example.warranymanagement.notifications.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WarrantyListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_list);

        findViewById(R.id.AddWarranty).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWarrantyActivity.class);
            startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_mine);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;

            } else if (itemId == R.id.nav_mine) {
                return true;

            } else if (itemId == R.id.nav_community) {
                Intent intent = new Intent(this, CommunityActivity.class);
                startActivity(intent);
                return true;

            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}