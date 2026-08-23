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
    // LOW STOCK LIMIT
    // ============================================================

    /*
     * Product is considered Low Stock when quantity is
     * 10 or less.
     *
     * 0       = Out of Stock
     * 1 - 10  = Low Stock
     * 11+     = In Stock
     */
    private static final int LOW_STOCK_LIMIT = 10;


    // ============================================================
    // UI
    // ============================================================

    private EditText etSearch;

    private TextView tvTotalProducts;
    private TextView tvStockValue;
    private TextView tvLowStock;

    private RecyclerView recyclerProducts;

    private FloatingActionButton fabAddProduct;


    // ============================================================
    // PRODUCT DATA
    // ============================================================

    /*
     * This is the complete product list loaded from Firebase.
     *
     * IMPORTANT:
     * Searching must NEVER modify this list permanently.
     */
    private final ArrayList<Product> productList =
            new ArrayList<>();


    private ProductAdapter adapter;


    // ============================================================
    // FIREBASE
    // ============================================================

    private DatabaseReference productsReference;


    // ============================================================
    // ACTIVITY CREATED
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


        fabAddProduct =
                findViewById(
                        R.id.fabAddProduct
                );


        // ========================================================
        // RECYCLER VIEW
        // ========================================================

        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(this)
        );


        recyclerProducts.setHasFixedSize(false);


        // ========================================================
        // PRODUCT ADAPTER
        // ========================================================

        adapter =
                new ProductAdapter(
                        ProductActivity.this,
                        productList
                );


        recyclerProducts.setAdapter(
                adapter
        );


        // ========================================================
        // FIREBASE AUTHENTICATION
        // ========================================================

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (currentUser == null) {

            Toast.makeText(
                    ProductActivity.this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // ========================================================
        // FIREBASE PRODUCT REFERENCE
        // ========================================================

        /*
         * Database structure:
         *
         * Products
         *    └── userId
         *          └── productId
         */

        productsReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(currentUser.getUid());


        // ========================================================
        // SEARCH
        // ========================================================

        setupSearch();


        // ========================================================
        // LOAD PRODUCTS
        // ========================================================

        loadProducts();


        // ========================================================
        // ADD PRODUCT
        // ========================================================

        fabAddProduct.setOnClickListener(
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
    // ON RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * Reload products whenever we return from:
         *
         * Add Product
         * Edit Product
         * Delete Product
         */
        if (productsReference != null) {

            loadProducts();
        }
    }


    // ============================================================
    // LOAD PRODUCTS FROM FIREBASE
    // ============================================================

    private void loadProducts() {

        if (productsReference == null) {
            return;
        }


        productsReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        // ====================================================
                        // TEMPORARY LIST
                        // ====================================================

                        ArrayList<Product> loadedProducts =
                                new ArrayList<>();


                        // ====================================================
                        // SUMMARY VALUES
                        // ====================================================

                        int lowStockCount = 0;

                        double totalStockValue = 0.0;


                        // ====================================================
                        // READ PRODUCTS
                        // ====================================================

                        for (DataSnapshot productSnapshot :
                                snapshot.getChildren()) {

                            Product product =
                                    createProductSafely(
                                            productSnapshot
                                    );


                            if (product == null) {
                                continue;
                            }


                            // =================================================
                            // SAVE FIREBASE KEY AS PRODUCT ID
                            // =================================================

                            product.setProductId(
                                    productSnapshot.getKey()
                            );


                            loadedProducts.add(
                                    product
                            );


                            // =================================================
                            // QUANTITY
                            // =================================================

                            int quantity =
                                    product.getQuantity();


                            // =================================================
                            // LOW STOCK
                            // =================================================

                            if (quantity > 0
                                    && quantity <= LOW_STOCK_LIMIT) {

                                lowStockCount++;
                            }


                            // =================================================
                            // STOCK VALUE
                            //
                            // IMPORTANT:
                            //
                            // Stock Value =
                            // Quantity × Cost Price
                            //
                            // NOT selling price.
                            // =================================================

                            double costPrice =
                                    product.getCostPrice();


                            totalStockValue +=
                                    quantity * costPrice;
                        }


                        // ====================================================
                        // UPDATE MAIN PRODUCT LIST
                        // ====================================================

                        productList.clear();

                        productList.addAll(
                                loadedProducts
                        );


                        // ====================================================
                        // UPDATE SUMMARY
                        // ====================================================

                        updateSummary(
                                loadedProducts.size(),
                                lowStockCount,
                                totalStockValue
                        );


                        // ====================================================
                        // UPDATE ADAPTER
                        //
                        // updateList() updates BOTH:
                        //
                        // list
                        // fullList
                        //
                        // This is required for search to work correctly.
                        // ====================================================

                        adapter.updateList(
                                loadedProducts
                        );


                        // ====================================================
                        // RESTORE SEARCH TEXT
                        // ====================================================

                        String currentSearch =
                                etSearch.getText()
                                        .toString()
                                        .trim();


                        if (!currentSearch.isEmpty()) {

                            adapter
                                    .getFilter()
                                    .filter(currentSearch);
                        }
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

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
    // UPDATE SUMMARY
    // ============================================================

    private void updateSummary(
            int totalProducts,
            int lowStockCount,
            double stockValue
    ) {

        // ========================================================
        // TOTAL PRODUCTS
        // ========================================================

        tvTotalProducts.setText(
                String.valueOf(
                        totalProducts
                )
        );


        // ========================================================
        // LOW STOCK
        // ========================================================

        tvLowStock.setText(
                String.valueOf(
                        lowStockCount
                )
        );


        // ========================================================
        // STOCK VALUE
        // ========================================================

        tvStockValue.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        stockValue
                )
        );
    }


    // ============================================================
    // SEARCH SETUP
    // ============================================================

    private void setupSearch() {

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                        // Nothing required
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        String query =
                                s == null
                                        ? ""
                                        : s.toString().trim();

                        adapter
                                .getFilter()
                                .filter(query);
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        // Nothing required
                    }
                }
        );
    }


    // ============================================================
    // CREATE PRODUCT SAFELY
    // ============================================================

    private Product createProductSafely(
            DataSnapshot snapshot
    ) {

        try {

            // ====================================================
            // PRODUCT NAME
            // ====================================================

            String productName =
                    getStringValue(
                            snapshot.child("productName")
                    );


            // ====================================================
            // CATEGORY ID
            // ====================================================

            String categoryId =
                    getStringValue(
                            snapshot.child("categoryId")
                    );


            // ====================================================
            // CATEGORY
            // ====================================================

            String category =
                    getStringValue(
                            snapshot.child("category")
                    );


            // ====================================================
            // BRAND
            // ====================================================

            String brandName =
                    getStringValue(
                            snapshot.child("brandName")
                    );


            // ====================================================
            // DESCRIPTION
            // ====================================================

            String description =
                    getStringValue(
                            snapshot.child("description")
                    );


            // ====================================================
            // IMAGE
            // ====================================================

            String imageUrl =
                    getStringValue(
                            snapshot.child("imageUrl")
                    );


            // ====================================================
            // QUANTITY
            // ====================================================

            int quantity =
                    getIntValue(
                            snapshot.child("quantity")
                    );


            // ====================================================
            // COST PRICE
            // ====================================================

            double costPrice =
                    getDoubleValue(
                            snapshot.child("costPrice")
                    );


            // ====================================================
            // SELLING PRICE
            // ====================================================

            double sellingPrice =
                    getDoubleValue(
                            snapshot.child("sellingPrice")
                    );


            // ====================================================
            // STOCK
            // ====================================================

            /*
             * Your Firebase database may contain stock as:
             *
             * String:
             * "In Stock"
             *
             * OR number:
             * 20
             *
             * Product.setStock(Object) safely handles both.
             */

            Object stockValue =
                    snapshot
                            .child("stock")
                            .getValue();


            // ====================================================
            // CREATE PRODUCT
            // ====================================================

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


            if (stockValue != null) {

                product.setStock(
                        stockValue
                );

            } else {

                /*
                 * Older products may not have a stock field.
                 *
                 * Quantity remains the source of truth.
                 */

                if (quantity <= 0) {

                    product.setStock(
                            "Out of Stock"
                    );

                } else if (
                        quantity <= LOW_STOCK_LIMIT
                ) {

                    product.setStock(
                            "Low Stock"
                    );

                } else {

                    product.setStock(
                            "In Stock"
                    );
                }
            }


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
                            + snapshot.getKey(),
                    Toast.LENGTH_SHORT
            ).show();

            return null;
        }
    }


    // ============================================================
    // SAFE STRING
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


        return String.valueOf(
                value
        );
    }


    // ============================================================
    // SAFE INTEGER
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


        // Firebase numeric value
        if (value instanceof Number) {

            return (
                    (Number) value
            ).intValue();
        }


        // String numeric value
        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            try {

                return (int)
                        Double.parseDouble(
                                String.valueOf(
                                        value
                                ).trim()
                        );

            } catch (Exception ignored) {

                return 0;
            }
        }
    }


    // ============================================================
    // SAFE DOUBLE
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


        // Firebase numeric value
        if (value instanceof Number) {

            return (
                    (Number) value
            ).doubleValue();
        }


        // String numeric value
        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            return 0.0;
        }
    }
}