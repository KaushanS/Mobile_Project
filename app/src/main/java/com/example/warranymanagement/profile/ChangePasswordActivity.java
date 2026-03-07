package com.example.warranymanagement.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;

public class ChangePasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> finish());
    }
}