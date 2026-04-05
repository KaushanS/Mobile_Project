package com.example.warranymanagement.profile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;
import com.example.warranymanagement.notifications.NotificationPublisher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private EditText etFullName;
    private EditText etEmail;
    private EditText etAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etEditFullName);
        etEmail = findViewById(R.id.etEditEmail);
        int ageFieldId = getResources().getIdentifier("etEditAge", "id", getPackageName());
        etAge = ageFieldId != 0 ? findViewById(ageFieldId) : null;

        loadUserData();

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            etFullName.setText(displayName != null && !displayName.isEmpty() ? displayName : "");
            etEmail.setText(email != null ? email : "");

            if (etAge != null) {
                db.collection("users")
                        .document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            Object ageValue = documentSnapshot.get("age");
                            if (ageValue instanceof Number) {
                                etAge.setText(String.valueOf(((Number) ageValue).intValue()));
                            } else if (ageValue instanceof String) {
                                etAge.setText((String) ageValue);
                            }
                        });
            }
        }
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etFullName.getText().toString().trim();
        if (etAge == null) {
            Toast.makeText(this, "Age field is missing in the layout", Toast.LENGTH_SHORT).show();
            return;
        }
        String ageText = etAge.getText().toString().trim();

        if (newName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (ageText.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid age");
            etAge.requestFocus();
            return;
        }

        if (age < 18) {
            etAge.setError("You must be at least 18 years old");
            etAge.requestFocus();
            return;
        }

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdate)
                .addOnSuccessListener(unused -> {
                    Map<String, Object> profileData = new HashMap<>();
                    profileData.put("name", newName);
                    profileData.put("age", age);

                    db.collection("users")
                            .document(currentUser.getUid())
                            .set(profileData, SetOptions.merge())
                            .addOnSuccessListener(setUnused -> {
                                NotificationPublisher.publishToUser(
                                        currentUser.getUid(),
                                        "profile",
                                        "Profile Updated",
                                        "Your profile was updated successfully"
                                );
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    "Profile name updated, but failed to save age: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}