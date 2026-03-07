package com.example.warranymanagement.warranty;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.warranymanagement.R;
import java.util.Calendar;
import java.util.Locale;

public class AddWarrantyActivity extends AppCompatActivity {

    private TextView PurchaseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_warranty);



    }
}