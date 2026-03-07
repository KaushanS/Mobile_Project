package com.example.warranymanagement.warranty;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;

public class WarrantyDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_detail);

        // Edit button
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditWarrantyActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            finish();
        });
    }
}