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

    RecyclerView recyclerCategory;
    EditText etSearchCategory;
    TextView tvTotalCategories;
    LinearLayout layoutEmpty;
    FloatingActionButton fabAddCategory;

    ArrayList<Category> categoryList;
    CategoryAdapter adapter;

    // Firebase references
    DatabaseReference databaseReference;
    DatabaseReference productsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);

        // --------------------------------
        // Initialize Views
        // --------------------------------

        recyclerCategory = findViewById(R.id.recyclerCategory);

        etSearchCategory = findViewById(R.id.etSearchCategory);

        tvTotalCategories = findViewById(R.id.tvTotalCategories);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        fabAddCategory = findViewById(R.id.fabAddCategory);


        // --------------------------------
        // RecyclerView
        // --------------------------------

        recyclerCategory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        categoryList = new ArrayList<>();


        adapter = new CategoryAdapter(
                this,
                categoryList,
                this
        );

        recyclerCategory.setAdapter(adapter);


        // --------------------------------
        // Firebase User
        // --------------------------------

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }


        // --------------------------------
        // Categories Reference
        // --------------------------------

        databaseReference =
                FirebaseDatabase.getInstance()
                        .getReference("Categories")
                        .child(user.getUid());


        // --------------------------------
        // Products Reference
        // --------------------------------

        productsReference =
                FirebaseDatabase.getInstance()
                        .getReference("Products")
                        .child(user.getUid());


        // --------------------------------
        // Load Categories
        // --------------------------------

        loadCategories();


        // --------------------------------
        // Add Category
        // --------------------------------

        fabAddCategory.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            CategoryActivity.this,
                            AddCategoryActivity.class
                    );

            startActivity(intent);
        });


        // --------------------------------
        // Search
        // --------------------------------

        etSearchCategory.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }


    // =========================================================
    // LOAD CATEGORIES
    // =========================================================

    private void loadCategories() {

        databaseReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        categoryList.clear();

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Category category =
                                    ds.getValue(Category.class);

                            if (category != null) {

                                category.setId(ds.getKey());
                                category.setProductCount(0);

                                categoryList.add(category);
                            }
                        }

                        loadProductCounts();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                CategoryActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // COUNT PRODUCTS
    // =========================================================

    private void loadProductCounts() {

        productsReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        // Reset counts
                        for (Category category :
                                categoryList) {

                            category.setProductCount(0);
                        }


                        // Loop through products
                        for (DataSnapshot productSnapshot :
                                snapshot.getChildren()) {

                            String categoryId =
                                    productSnapshot
                                            .child("categoryId")
                                            .getValue(String.class);

                            String categoryName =
                                    productSnapshot
                                            .child("category")
                                            .getValue(String.class);


                            // Find matching category
                            for (Category category :
                                    categoryList) {

                                boolean matched = false;


                                // --------------------------------
                                // New products
                                // Match using categoryId
                                // --------------------------------

                                if (categoryId != null
                                        && !categoryId.isEmpty()
                                        && categoryId.equals(
                                        category.getId())) {

                                    matched = true;
                                }


                                // --------------------------------
                                // Old products
                                // Match using category name
                                // --------------------------------

                                if (!matched
                                        && (categoryId == null
                                        || categoryId.isEmpty())
                                        && categoryName != null
                                        && categoryName.equalsIgnoreCase(
                                        category.getCategoryName())) {

                                    matched = true;
                                }


                                if (matched) {

                                    category.setProductCount(
                                            category.getProductCount() + 1
                                    );

                                    break;
                                }
                            }
                        }


                        // --------------------------------
                        // Refresh Category List
                        // --------------------------------
                        adapter.refreshList(new ArrayList<>(categoryList));

                        tvTotalCategories.setText(
                                String.valueOf(categoryList.size())
                        );

                        // --------------------------------
                        // Empty State
                        // --------------------------------

                        if (categoryList.isEmpty()) {

                            layoutEmpty.setVisibility(
                                    View.VISIBLE
                            );

                            recyclerCategory.setVisibility(
                                    View.GONE
                            );

                        } else {

                            layoutEmpty.setVisibility(
                                    View.GONE
                            );

                            recyclerCategory.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                CategoryActivity.this,
                                "Product error: "
                                        + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // =========================================================
    // EDIT CATEGORY
    // =========================================================

    @Override
    public void onEdit(Category category) {

        Intent intent =
                new Intent(
                        CategoryActivity.this,
                        EditCategoryActivity.class
                );

        intent.putExtra(
                "categoryId",
                category.getId()
        );

        intent.putExtra(
                "categoryName",
                category.getCategoryName()
        );

        intent.putExtra(
                "description",
                category.getDescription()
        );

        intent.putExtra(
                "status",
                category.getStatus()
        );

        startActivity(intent);
    }


    // =========================================================
    // DELETE CATEGORY
    // =========================================================

    @Override
    public void onDelete(Category category) {

        if (category.getId() == null) {

            Toast.makeText(
                    this,
                    "Invalid category",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        databaseReference
                .child(category.getId())
                .removeValue()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Category Deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Delete failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}