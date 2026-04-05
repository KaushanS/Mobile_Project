package com.example.warranymanagement.warranty;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddWarrantyActivity extends AppCompatActivity {

    private static final String DEFAULT_PERIOD = "2 Years";
    private static final String DEFAULT_CATEGORY = "Electronics";
    private static final String CLOUDINARY_CLOUD_NAME = "dfarss7cg";
    private static final String CLOUDINARY_UPLOAD_PRESET = "warranty_upload";

    private TextView tvPurchaseDate;
    private TextView tvExpiryDate;
    private TextView tvWarrantyPeriodValue;
    private EditText etProductName;
    private EditText etStoreName;
    private EditText etLocation;
    private AutoCompleteTextView actWarrantyPeriod;
    private AutoCompleteTextView actCategory;
    private ImageView ivWarrantyPhoto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Calendar selectedPurchaseCal;
    private String expiryDateIso;
    private String selectedWarrantyPeriod = DEFAULT_PERIOD;
    private String selectedCategory = DEFAULT_CATEGORY;
    private Uri selectedPhotoUri;

    private final SimpleDateFormat displayFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    if (ivWarrantyPhoto != null) {
                        ivWarrantyPhoto.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_warranty);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etProductName = findViewById(R.id.etProductName);
        etStoreName = findViewById(R.id.etStoreName);
        etLocation = findViewById(R.id.etLocation);
        tvPurchaseDate = findViewById(R.id.tvPurchaseDate);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvWarrantyPeriodValue = findViewById(R.id.tvWarrantyPeriodValue);
        actWarrantyPeriod = findViewById(R.id.actWarrantyPeriod);
        actCategory = findViewById(R.id.actCategory);
        ivWarrantyPhoto = findViewById(R.id.ivWarrantyPhoto);

        selectedPurchaseCal = Calendar.getInstance();
        setupPhotoUpload();
        setupWarrantyPeriodDropdown();
        setupCategoryDropdown();
        updateDateUI();

        findViewById(R.id.llPurchaseDate).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveWarranty());
    }

    private void setupPhotoUpload() {
        findViewById(R.id.rlPhotoUpload).setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
    }

    private void setupWarrantyPeriodDropdown() {
        if (actWarrantyPeriod == null) return;

        String[] periods = new String[]{"1 Month", "3 Months", "6 Months", "1 Year", "2 Years", "3 Years", "5 Years"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
        actWarrantyPeriod.setAdapter(adapter);
        actWarrantyPeriod.setKeyListener(null);
        actWarrantyPeriod.setFocusable(false);
        actWarrantyPeriod.setClickable(true);
        actWarrantyPeriod.setText(DEFAULT_PERIOD, false);
        applyWarrantyPeriod(DEFAULT_PERIOD);

        actWarrantyPeriod.setOnClickListener(v -> actWarrantyPeriod.showDropDown());
        actWarrantyPeriod.setOnItemClickListener((parent, view, position, id) -> applyWarrantyPeriod(periods[position]));
    }

    private void setupCategoryDropdown() {
        if (actCategory == null) return;

        String[] categories = new String[]{"Electronics", "Appliances", "Vehicle"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(adapter);
        actCategory.setKeyListener(null);
        actCategory.setFocusable(false);
        actCategory.setClickable(true);
        actCategory.setText(DEFAULT_CATEGORY, false);
        selectedCategory = DEFAULT_CATEGORY;

        actCategory.setOnClickListener(v -> actCategory.showDropDown());
        actCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories[position];
            actCategory.setText(selectedCategory, false);
        });
    }

    private void applyWarrantyPeriod(String period) {
        if (period == null) period = DEFAULT_PERIOD;
        selectedWarrantyPeriod = period.trim();
        updateDateUI();
    }

    private void showDatePicker() {
        int year = selectedPurchaseCal.get(Calendar.YEAR);
        int month = selectedPurchaseCal.get(Calendar.MONTH);
        int day = selectedPurchaseCal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            selectedPurchaseCal.set(Calendar.YEAR, y);
            selectedPurchaseCal.set(Calendar.MONTH, m);
            selectedPurchaseCal.set(Calendar.DAY_OF_MONTH, d);
            updateDateUI();
        }, year, month, day).show();
    }

    private void updateDateUI() {
        if (tvPurchaseDate != null) {
            tvPurchaseDate.setText(displayFmt.format(selectedPurchaseCal.getTime()));
        }

        Calendar expiryCal = (Calendar) selectedPurchaseCal.clone();
        addWarrantyPeriod(expiryCal, selectedWarrantyPeriod);
        expiryDateIso = isoFmt.format(expiryCal.getTime());

        if (tvWarrantyPeriodValue != null) {
            tvWarrantyPeriodValue.setText(selectedWarrantyPeriod);
        }

        if (tvExpiryDate != null) {
            tvExpiryDate.setText(displayFmt.format(expiryCal.getTime()));
        }
    }

    private void addWarrantyPeriod(Calendar expiryCal, String period) {
        String value = period == null ? DEFAULT_PERIOD : period.trim().toLowerCase(Locale.US);

        if (value.contains("1 month")) {
            expiryCal.add(Calendar.MONTH, 1);
        } else if (value.contains("3 month")) {
            expiryCal.add(Calendar.MONTH, 3);
        } else if (value.contains("6 month")) {
            expiryCal.add(Calendar.MONTH, 6);
        } else if (value.contains("1 year")) {
            expiryCal.add(Calendar.YEAR, 1);
        } else if (value.contains("2 year")) {
            expiryCal.add(Calendar.YEAR, 2);
        } else if (value.contains("3 year")) {
            expiryCal.add(Calendar.YEAR, 3);
        } else if (value.contains("5 year")) {
            expiryCal.add(Calendar.YEAR, 5);
        } else {
            expiryCal.add(Calendar.YEAR, 2);
        }
    }

    private void saveWarranty() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String productName = etProductName.getText().toString().trim();
        if (TextUtils.isEmpty(productName)) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedCategory)) {
            selectedCategory = DEFAULT_CATEGORY;
        }

        if (selectedPhotoUri == null) {
            saveWarrantyToFirestore(null);
            return;
        }

        uploadPhotoToCloudinary(selectedPhotoUri);
    }

    private void uploadPhotoToCloudinary(Uri photoUri) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload";
                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String boundary = "----WarrantyBoundary" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = connection.getOutputStream()) {
                    writeTextPart(os, boundary, "upload_preset", CLOUDINARY_UPLOAD_PRESET);
                    writeFilePart(os, boundary, "file", "warranty_" + System.currentTimeMillis() + ".jpg", photoUri);
                    os.write(("--" + boundary + "--\r\n").getBytes());
                    os.flush();
                }

                int statusCode = connection.getResponseCode();
                InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = readFully(responseStream);
                if (statusCode >= 200 && statusCode < 300) {
                    JSONObject json = new JSONObject(response);
                    String secureUrl = json.optString("secure_url", null);
                    runOnUiThread(() -> {
                        if (!TextUtils.isEmpty(secureUrl)) {
                            saveWarrantyToFirestore(secureUrl);
                        } else {
                            Toast.makeText(this, "Photo upload failed: Invalid Cloudinary response", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Photo upload failed: " + response, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    @SuppressWarnings("SameParameterValue")
    private void writeTextPart(OutputStream os, String boundary, String name, String value) throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        os.write((value + "\r\n").getBytes());
    }

    @SuppressWarnings("SameParameterValue")
    private void writeFilePart(OutputStream os, String boundary, String fieldName, String fileName, Uri fileUri) throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        os.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());

        try (InputStream is = getContentResolver().openInputStream(fileUri)) {
            if (is == null) throw new RuntimeException("Unable to read selected image");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }

        os.write("\r\n".getBytes());
    }

    private String readFully(InputStream inputStream) throws Exception {
        if (inputStream == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        return bos.toString();
    }

    private void saveWarrantyToFirestore(String photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String productName = etProductName.getText().toString().trim();
        String storeName = etStoreName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        Map<String, Object> warranty = new HashMap<>();
        warranty.put("productName", productName);
        warranty.put("storeName", storeName);
        warranty.put("location", location);
        warranty.put("category", selectedCategory);
        warranty.put("warrantyPeriod", selectedWarrantyPeriod);
        warranty.put("purchaseDate", isoFmt.format(selectedPurchaseCal.getTime()));
        warranty.put("expiryDate", expiryDateIso);
        warranty.put("photoUrl", photoUrl);
        warranty.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .collection("warranties")
                .add(warranty)
                .addOnSuccessListener(documentReference -> {
                    NotificationPublisher.publishToUser(
                            user.getUid(),
                            "add",
                            "Warranty Added",
                            productName + " warranty saved successfully"
                    );
                    Toast.makeText(this, "Warranty saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save warranty", Toast.LENGTH_SHORT).show());
    }
}
