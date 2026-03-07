package com.example.warranymanagement.community;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;

public class WriteReviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        findViewById(R.id.btnPostReview).setOnClickListener(v -> finish());
    }
}