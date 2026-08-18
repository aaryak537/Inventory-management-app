package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import java.util.Locale;

public class ProductActivity extends AppCompatActivity {

    // ============================================================
    // UI
    // ============================================================

    private EditText etSearch;

    private TextView tvTotalProducts;
    private TextView tvStockValue;
    private TextView tvLowStock;

    private RecyclerView recyclerProducts;

    private FloatingActionButton fabAdd;


    // ============================================================
    // PRODUCT LIST
    // ============================================================

    private ArrayList<Product> productList;

    private ProductAdapter adapter;


    // ============================================================
    // FIREBASE
    // ============================================================

    private DatabaseReference databaseReference;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_product
        );


        // ========================================================
        // INITIALIZE VIEWS
        // ========================================================

        fabAdd =
                findViewById(
                        R.id.fabAddProduct
                );

        etSearch =
                findViewById(
                        R.id.etSearch
                );

        tvTotalProducts =
                findViewById(
                        R.id.tvTotalProducts
                );

        tvStockValue =
                findViewById(
                        R.id.tvStockValue
                );

        tvLowStock =
                findViewById(
                        R.id.tvLowStock
                );

        recyclerProducts =
                findViewById(
                        R.id.recyclerProducts
                );


        // ========================================================
        // PRODUCT LIST
        // ========================================================

        productList =
                new ArrayList<>();


        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new ProductAdapter(
                        this,
                        productList
                );


        recyclerProducts.setAdapter(
                adapter
        );


        // ========================================================
        // FIREBASE USER
        // ========================================================

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (user == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // ========================================================
        // FIREBASE PRODUCTS REFERENCE
        // ========================================================

        databaseReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(user.getUid());


        // ========================================================
        // LOAD PRODUCTS
        // ========================================================

        loadProducts();


        // ========================================================
        // SEARCH
        // ========================================================

        searchProduct();


        // ========================================================
        // ADD PRODUCT
        // ========================================================

        fabAdd.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ProductActivity.this,
                                    AddProActivity.class
                            );

                    startActivity(intent);
                }
        );
    }


    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (databaseReference != null) {

            loadProducts();
        }
    }


    // ============================================================
    // LOAD PRODUCTS
    // ============================================================

    private void loadProducts() {

        if (databaseReference == null) {
            return;
        }


        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        productList.clear();


                        double stockValue = 0.0;

                        int lowStockCount = 0;


                        // ====================================================
                        // READ EACH PRODUCT SAFELY
                        // ====================================================

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Product product =
                                    createProductSafely(ds);


                            if (product == null) {
                                continue;
                            }


                            // Firebase key
                            product.setProductId(
                                    ds.getKey()
                            );


                            productList.add(
                                    product
                            );


                            // =================================================
                            // STOCK VALUE
                            // =================================================
                            // Stock value = quantity × cost price
                            // =================================================

                            int quantity =
                                    product.getQuantity();


                            double costPrice =
                                    product.getCostPrice();


                            stockValue +=
                                    quantity * costPrice;


                            // =================================================
                            // LOW STOCK
                            // =================================================

                            if (quantity <= 10) {

                                lowStockCount++;
                            }
                        }


                        // ====================================================
                        // TOTAL PRODUCTS
                        // ====================================================

                        int totalProducts =
                                productList.size();


                        tvTotalProducts.setText(
                                String.valueOf(
                                        totalProducts
                                )
                        );


                        // ====================================================
                        // STOCK VALUE
                        // ====================================================

                        tvStockValue.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "₹%.2f",
                                        stockValue
                                )
                        );


                        // ====================================================
                        // LOW STOCK
                        // ====================================================

                        tvLowStock.setText(
                                String.valueOf(
                                        lowStockCount
                                )
                        );


                        // ====================================================
                        // UPDATE RECYCLER
                        // ====================================================

                        adapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ProductActivity.this,
                                "Failed to load products: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // SAFE PRODUCT CREATION
    // ============================================================

    private Product createProductSafely(
            DataSnapshot ds
    ) {

        try {

            String productName =
                    getStringValue(
                            ds.child("productName")
                    );


            String categoryId =
                    getStringValue(
                            ds.child("categoryId")
                    );


            String category =
                    getStringValue(
                            ds.child("category")
                    );


            String brandName =
                    getStringValue(
                            ds.child("brandName")
                    );


            String description =
                    getStringValue(
                            ds.child("description")
                    );


            String imageUrl =
                    getStringValue(
                            ds.child("imageUrl")
                    );


            int quantity =
                    getIntValue(
                            ds.child("quantity")
                    );


            double costPrice =
                    getDoubleValue(
                            ds.child("costPrice")
                    );


            double sellingPrice =
                    getDoubleValue(
                            ds.child("sellingPrice")
                    );


            int stock =
                    getIntValue(
                            ds.child("stock")
                    );


            // =================================================
            // IF STOCK DOESN'T EXIST
            // USE QUANTITY
            // =================================================

            if (!ds.hasChild("stock")) {

                stock = quantity;
            }


            Product product =
                    new Product();


            product.setProductName(
                    productName
            );

            product.setCategoryId(
                    categoryId
            );

            product.setCategory(
                    category
            );

            product.setQuantity(
                    quantity
            );

            product.setBrandName(
                    brandName
            );

            product.setCostPrice(
                    costPrice
            );

            product.setSellingPrice(
                    sellingPrice
            );

            product.setStock(
                    stock
            );

            product.setDescription(
                    description
            );

            product.setImageUrl(
                    imageUrl
            );


            return product;

        } catch (Exception e) {

            Toast.makeText(
                    ProductActivity.this,
                    "Unable to read product: "
                            + ds.getKey(),
                    Toast.LENGTH_SHORT
            ).show();

            return null;
        }
    }


    // ============================================================
    // SAFE STRING READER
    // ============================================================

    private String getStringValue(
            DataSnapshot snapshot
    ) {

        if (!snapshot.exists()) {

            return "";
        }


        Object value =
                snapshot.getValue();


        if (value == null) {

            return "";
        }


        // Normal String
        if (value instanceof String) {

            return (String) value;
        }


        // Firebase number
        if (value instanceof Long) {

            return String.valueOf(
                    value
            );
        }


        if (value instanceof Double) {

            return String.valueOf(
                    value
            );
        }


        if (value instanceof Integer) {

            return String.valueOf(
                    value
            );
        }


        if (value instanceof Float) {

            return String.valueOf(
                    value
            );
        }


        return String.valueOf(
                value
        );
    }


    // ============================================================
    // SAFE INTEGER READER
    // ============================================================

    private int getIntValue(
            DataSnapshot snapshot
    ) {

        if (!snapshot.exists()) {

            return 0;
        }


        Object value =
                snapshot.getValue();


        if (value == null) {

            return 0;
        }


        if (value instanceof Long) {

            return ((Long) value).intValue();
        }


        if (value instanceof Integer) {

            return (Integer) value;
        }


        if (value instanceof Double) {

            return ((Double) value).intValue();
        }


        if (value instanceof Float) {

            return ((Float) value).intValue();
        }


        if (value instanceof String) {

            try {

                return Integer.parseInt(
                        ((String) value).trim()
                );

            } catch (NumberFormatException e) {

                try {

                    return (int) Double.parseDouble(
                            ((String) value).trim()
                    );

                } catch (NumberFormatException ignored) {

                    return 0;
                }
            }
        }


        return 0;
    }


    // ============================================================
    // SAFE DOUBLE READER
    // ============================================================

    private double getDoubleValue(
            DataSnapshot snapshot
    ) {

        if (!snapshot.exists()) {

            return 0.0;
        }


        Object value =
                snapshot.getValue();


        if (value == null) {

            return 0.0;
        }


        if (value instanceof Double) {

            return (Double) value;
        }


        if (value instanceof Long) {

            return ((Long) value).doubleValue();
        }


        if (value instanceof Integer) {

            return ((Integer) value).doubleValue();
        }


        if (value instanceof Float) {

            return ((Float) value).doubleValue();
        }


        if (value instanceof String) {

            try {

                return Double.parseDouble(
                        ((String) value).trim()
                );

            } catch (NumberFormatException e) {

                return 0.0;
            }
        }


        return 0.0;
    }


    // ============================================================
    // SEARCH PRODUCT
    // ============================================================

    private void searchProduct() {

        etSearch.addTextChangedListener(
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

                        adapter
                                .getFilter()
                                .filter(s);
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }
}