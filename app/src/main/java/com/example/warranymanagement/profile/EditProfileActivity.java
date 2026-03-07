package com.example.warranymanagement.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> finish());
    }
}