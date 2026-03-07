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


    }
}
