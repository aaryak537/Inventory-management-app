package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity
        implements CategoryAdapter.OnCategoryActionListener {

    private RecyclerView recyclerCategory;
    private EditText etSearchCategory;
    private TextView tvTotalCategories;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddCategory;

    private ArrayList<Category> categoryList;
    private CategoryAdapter adapter;

    private DatabaseReference databaseReference;
    private DatabaseReference productsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);

        recyclerCategory = findViewById(R.id.recyclerCategory);
        etSearchCategory = findViewById(R.id.etSearchCategory);
        tvTotalCategories = findViewById(R.id.tvTotalCategories);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        recyclerCategory.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();

        adapter = new CategoryAdapter(this, categoryList, this);

        recyclerCategory.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Categories")
                .child(uid);

        productsReference = FirebaseDatabase.getInstance().getReference("Products")
                .child(uid);

        loadCategories();

        fabAddCategory.setOnClickListener(v -> {

            Intent intent = new Intent(CategoryActivity.this,
                    AddCategoryActivity.class
            );
            startActivity(intent);
        });

        etSearchCategory.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {}
                }
        );
    }

    private void loadCategories() {

        databaseReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        categoryList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Category category = ds.getValue(Category.class);

                            if (category != null) {
                                category.setId(ds.getKey());

                                if (category.getProductCount() < 0) {
                                    category.setProductCount(0);
                                }
                                categoryList.add(category);
                            }
                        }
                        adapter.refreshList(new ArrayList<>(categoryList));

                        tvTotalCategories.setText(String.valueOf(categoryList.size()));

                        if (categoryList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);

                            recyclerCategory.setVisibility(View.GONE);
                        } else {

                            layoutEmpty.setVisibility(View.GONE);

                            recyclerCategory.setVisibility(View.VISIBLE);
                        }
                        loadProductCounts();
                    }
                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(CategoryActivity.this,
                                "Failed to load categories: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void loadProductCounts() {

        productsReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        for (Category category : categoryList) {
                            category.setProductCount(0);
                        }

                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            String categoryId = productSnapshot.child("categoryId")
                                    .getValue(String.class);

                            String categoryName = productSnapshot.child("category")
                                    .getValue(String.class);

                            for (Category category : categoryList) {
                                boolean matched = false;

                                if (categoryId != null && !categoryId.isEmpty()
                                        && categoryId.equals(category.getId())) {
                                    matched = true;
                                }

                                if (!matched && categoryName != null
                                        && !categoryName.isEmpty()
                                        && categoryName.equalsIgnoreCase(
                                        category.getCategoryName())) {
                                    matched = true;
                                }
                                if (matched) {
                                    category.setProductCount(category.getProductCount() + 1);
                                    break;
                                }
                            }
                        }
                        adapter.refreshList(new ArrayList<>(categoryList));

                        tvTotalCategories.setText(String.valueOf(categoryList.size()));

                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(CategoryActivity.this, "Product error: "
                                + error.getMessage(), Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void updateEmptyState() {

        if (categoryList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);

            recyclerCategory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);

            recyclerCategory.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onEdit(Category category) {

        Intent intent = new Intent(CategoryActivity.this,
                EditCategoryActivity.class
        );

        intent.putExtra("categoryId", category.getId());

        intent.putExtra("categoryName", category.getCategoryName());

        intent.putExtra("description", category.getDescription());

        intent.putExtra("status", category.getStatus());

        startActivity(intent);
    }





    @Override
    public void onDelete(Category category) {

        if (category.getId() == null || category.getId().isEmpty()) {

            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(category.getId()).removeValue()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Category Deleted", Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}