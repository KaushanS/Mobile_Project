package com.example.warranymanagement.warranty;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditWarrantyActivity extends AppCompatActivity {

    private static final String CLOUDINARY_CLOUD_NAME = "dfarss7cg";
    private static final String CLOUDINARY_UPLOAD_PRESET = "warranty_upload";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String warrantyId;

    private EditText etProductName;
    private EditText etStoreName;
    private AutoCompleteTextView actCategory;
    private AutoCompleteTextView actWarrantyPeriod;
    private TextView tvPurchaseDate;
    private TextView tvExpiryDate;
    private ImageView ivWarrantyCardEdit;

    private String selectedCategory = "Electronics";
    private String selectedWarrantyPeriod = "2 Years";
    private String currentPurchaseDateIso = "";
    private String currentPhotoUrl = "";
    private Uri selectedPhotoUri;

    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    if (ivWarrantyCardEdit != null) {
                        ivWarrantyCardEdit.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_warranty);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        warrantyId = getIntent().getStringExtra(WarrantyDetailActivity.EXTRA_WARRANTY_ID);

        bindViews();
        setupDropdowns();
        loadWarranty();
    }

    private void bindViews() {
        etProductName = findViewById(R.id.etProductName);
        etStoreName = findViewById(R.id.etStoreName);
        actCategory = findViewById(R.id.actCategory);
        actWarrantyPeriod = findViewById(R.id.actWarrantyPeriod);
        tvPurchaseDate = findViewById(R.id.tvPurchaseDate);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
    }

    private void setupDropdowns() {
        if (actCategory != null) {
            String[] categories = new String[]{"Electronics", "Appliances", "Vehicle"};
            ArrayAdapter<String> catAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
            actCategory.setAdapter(catAdapter);
            actCategory.setKeyListener(null);
            actCategory.setFocusable(false);
            actCategory.setClickable(true);
            actCategory.setOnClickListener(v -> actCategory.showDropDown());
            actCategory.setOnItemClickListener((parent, view, position, id) -> {
                selectedCategory = categories[position];
                actCategory.setText(selectedCategory, false);
            });
        }

        if (actWarrantyPeriod != null) {
            String[] periods = new String[]{"1 Month", "3 Months", "6 Months", "1 Year", "2 Years", "3 Years", "5 Years"};
            ArrayAdapter<String> periodAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
            actWarrantyPeriod.setAdapter(periodAdapter);
            actWarrantyPeriod.setKeyListener(null);
            actWarrantyPeriod.setFocusable(false);
            actWarrantyPeriod.setClickable(true);
            actWarrantyPeriod.setOnClickListener(v -> actWarrantyPeriod.showDropDown());
            actWarrantyPeriod.setOnItemClickListener((parent, view, position, id) -> {
                selectedWarrantyPeriod = periods[position];
                actWarrantyPeriod.setText(selectedWarrantyPeriod, false);
                renderExpiryFromInputs();
            });
        }
    }


    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (!TextUtils.isEmpty(currentPurchaseDateIso)) {
            try {
                cal.setTime(isoFmt.parse(currentPurchaseDateIso));
            } catch (Exception ignored) {
            }
        }

        new DatePickerDialog(this, (dialog, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            currentPurchaseDateIso = isoFmt.format(cal.getTime());
            if (tvPurchaseDate != null) {
                tvPurchaseDate.setText(displayFmt.format(cal.getTime()));
            }
            renderExpiryFromInputs();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadWarranty() {
        if (currentUser == null || TextUtils.isEmpty(warrantyId)) {
            Toast.makeText(this, "Warranty not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("warranties")
                .document(warrantyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Warranty not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    if (etProductName != null) etProductName.setText(value(doc.getString("productName")));
                    if (etStoreName != null) etStoreName.setText(value(doc.getString("storeName")));

                    currentPurchaseDateIso = value(doc.getString("purchaseDate"));
                    selectedWarrantyPeriod = value(doc.getString("warrantyPeriod"));
                    selectedCategory = value(doc.getString("category"));
                    currentPhotoUrl = value(doc.getString("photoUrl"));

                    if (TextUtils.isEmpty(selectedWarrantyPeriod)) selectedWarrantyPeriod = "2 Years";
                    if (TextUtils.isEmpty(selectedCategory)) selectedCategory = "Electronics";

                    if (tvPurchaseDate != null) tvPurchaseDate.setText(formatDate(currentPurchaseDateIso));
                    if (actWarrantyPeriod != null) actWarrantyPeriod.setText(selectedWarrantyPeriod, false);
                    if (actCategory != null) actCategory.setText(selectedCategory, false);

                    renderExpiryFromInputs();

                    if (!TextUtils.isEmpty(currentPhotoUrl) && ivWarrantyCardEdit != null) {
                        loadImageFromUrl(currentPhotoUrl, ivWarrantyCardEdit);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load warranty", Toast.LENGTH_SHORT).show());
    }

    private void renderExpiryFromInputs() {
        String purchaseIso = TextUtils.isEmpty(currentPurchaseDateIso)
                ? isoFmt.format(Calendar.getInstance().getTime())
                : currentPurchaseDateIso;
        String expiryIso = calculateExpiryDateIso(purchaseIso, selectedWarrantyPeriod);
        if (tvExpiryDate != null) {
            tvExpiryDate.setText(formatDate(expiryIso));
        }
    }

    private void saveChanges() {
        if (currentUser == null || TextUtils.isEmpty(warrantyId)) {
            Toast.makeText(this, "Warranty not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String productName = etProductName == null ? "" : etProductName.getText().toString().trim();
        String storeName = etStoreName == null ? "" : etStoreName.getText().toString().trim();

        if (TextUtils.isEmpty(productName)) {
            if (etProductName != null) {
                etProductName.setError("Product name is required");
                etProductName.requestFocus();
            }
            return;
        }

        if (TextUtils.isEmpty(storeName)) {
            if (etStoreName != null) {
                etStoreName.setError("Store name is required");
                etStoreName.requestFocus();
            }
            return;
        }

        String purchaseDateIso = TextUtils.isEmpty(currentPurchaseDateIso)
                ? isoFmt.format(Calendar.getInstance().getTime())
                : currentPurchaseDateIso;

        if (selectedPhotoUri != null) {
            uploadPhotoThenSave(productName, storeName, purchaseDateIso);
        } else {
            saveToFirestore(productName, storeName, purchaseDateIso, currentPhotoUrl);
        }
    }

    private void uploadPhotoThenSave(String productName, String storeName, String purchaseDateIso) {
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
                    writeFilePart(os, boundary, "file", "warranty_" + System.currentTimeMillis() + ".jpg", selectedPhotoUri);
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
                    String secureUrl = json.optString("secure_url", "");
                    runOnUiThread(() -> saveToFirestore(productName, storeName, purchaseDateIso, secureUrl));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Photo upload failed", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void saveToFirestore(String productName, String storeName, String purchaseDateIso, String photoUrl) {
        String expiryDateIso = calculateExpiryDateIso(purchaseDateIso, selectedWarrantyPeriod);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("warranties")
                .document(warrantyId)
                .update(
                        "productName", productName,
                        "storeName", storeName,
                        "category", selectedCategory,
                        "warrantyPeriod", selectedWarrantyPeriod,
                        "purchaseDate", purchaseDateIso,
                        "expiryDate", expiryDateIso,
                        "photoUrl", photoUrl
                )
                .addOnSuccessListener(unused -> {
                    NotificationPublisher.publishToUser(
                            currentUser.getUid(),
                            "edit",
                            "Warranty Updated",
                            productName + " warranty updated"
                    );
                    Toast.makeText(this, "Warranty updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update warranty", Toast.LENGTH_SHORT).show());
    }

    private String calculateExpiryDateIso(String purchaseDateIso, String period) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(isoFmt.parse(purchaseDateIso));

            String p = period == null ? "2 years" : period.toLowerCase(Locale.US);
            if (p.contains("1 month")) cal.add(Calendar.MONTH, 1);
            else if (p.contains("3 month")) cal.add(Calendar.MONTH, 3);
            else if (p.contains("6 month")) cal.add(Calendar.MONTH, 6);
            else if (p.contains("1 year")) cal.add(Calendar.YEAR, 1);
            else if (p.contains("2 year")) cal.add(Calendar.YEAR, 2);
            else if (p.contains("3 year")) cal.add(Calendar.YEAR, 3);
            else if (p.contains("5 year")) cal.add(Calendar.YEAR, 5);
            else cal.add(Calendar.YEAR, 2);

            return isoFmt.format(cal.getTime());
        } catch (Exception e) {
            return purchaseDateIso;
        }
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.setRequestProperty("Accept", "image/*");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.connect();

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    return;
                }

                input = connection.getInputStream();
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                if (bitmap != null) {
                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    if (input != null) input.close();
                } catch (Exception ignored) {
                }
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void writeTextPart(OutputStream os, String boundary, String name, String value) throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        os.write((value + "\r\n").getBytes());
    }

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

    private String formatDate(String isoDate) {
        if (TextUtils.isEmpty(isoDate)) return "";
        try {
            return displayFmt.format(isoFmt.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }

    private String value(String s) {
        return s == null ? "" : s;
    }
}