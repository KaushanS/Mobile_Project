package com.example.warranymanagement.warranty;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warranymanagement.R;
import com.example.warranymanagement.community.CommunityActivity;
import com.example.warranymanagement.home.HomeActivity;
import com.example.warranymanagement.settings.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WarrantyListActivity extends AppCompatActivity {

    private final List<WarrantyItem> allWarranties = new ArrayList<>();
    private final List<WarrantyItem> filteredWarranties = new ArrayList<>();

    private WarrantyListAdapter adapter;
    private String selectedCategory = "All";
    private String searchText = "";
    private TextView tvFilterAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_list);

        RecyclerView rvWarranties = findViewById(R.id.rvWarranties);
        rvWarranties.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WarrantyListAdapter();
        rvWarranties.setAdapter(adapter);
        adapter.setOnWarrantyClickListener(item -> {
            Intent intent = new Intent(this, WarrantyDetailActivity.class);
            intent.putExtra(WarrantyDetailActivity.EXTRA_WARRANTY_ID, item.getId());
            startActivity(intent);
        });

        setupActions();
        setupSearch();
        setupCategoryFilters();
        setupBottomNavigation();

        loadUserWarranties();
    }

    private void setupActions() {
        findViewById(R.id.AddWarranty).setOnClickListener(v -> startActivity(new Intent(this, AddWarrantyActivity.class)));
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s == null ? "" : s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilters() {
        tvFilterAll = findViewById(R.id.tvFilterAll);

        findViewById(R.id.filterAll).setOnClickListener(v -> { selectedCategory = "All"; applyFilters(); });
        findViewById(R.id.filterElectronics).setOnClickListener(v -> { selectedCategory = "Electronics"; applyFilters(); });
        findViewById(R.id.filterAppliances).setOnClickListener(v -> { selectedCategory = "Appliances"; applyFilters(); });
        findViewById(R.id.filterVehicle).setOnClickListener(v -> { selectedCategory = "Vehicle"; applyFilters(); });
    }

    private void loadUserWarranties() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            allWarranties.clear();
            filteredWarranties.clear();
            adapter.submitList(filteredWarranties);
            if (tvFilterAll != null) tvFilterAll.setText("All\n(0)");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("warranties")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allWarranties.clear();
                    querySnapshot.getDocuments().forEach(doc -> {
                        WarrantyItem item = doc.toObject(WarrantyItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            allWarranties.add(item);
                        }
                    });
                    applyFilters();
                });
    }

    private void applyFilters() {
        filteredWarranties.clear();
        String q = searchText.toLowerCase(Locale.US);

        for (WarrantyItem item : allWarranties) {
            String category = safe(item.getCategory());
            String product = safe(item.getProductName()).toLowerCase(Locale.US);
            String store = safe(item.getStoreName()).toLowerCase(Locale.US);

            boolean matchCategory = "All".equals(selectedCategory) || category.equalsIgnoreCase(selectedCategory);
            boolean matchSearch = q.isEmpty() || product.contains(q) || store.contains(q);

            if (matchCategory && matchSearch) {
                filteredWarranties.add(item);
            }
        }

        if (tvFilterAll != null) {
            tvFilterAll.setText("All\n(" + allWarranties.size() + ")");
        }

        adapter.submitList(filteredWarranties);
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_mine);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_mine) {
                return true;
            } else if (itemId == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserWarranties();
    }
}