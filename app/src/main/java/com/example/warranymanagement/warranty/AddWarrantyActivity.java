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


        PurchaseDate = findViewById(R.id.tvPurchaseDate);
        findViewById(R.id.llPurchaseDate).setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnSave).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, monthOfYear, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, selectedYear);
            if (PurchaseDate != null) {
                PurchaseDate.setText(date);
            }
        }, year, month, day);
        datePickerDialog.show();
    }
}