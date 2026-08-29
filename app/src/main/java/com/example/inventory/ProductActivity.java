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






    private static final int LOW_STOCK_LIMIT = 10;






    private EditText etSearch;

    private TextView tvTotalProducts;
    private TextView tvStockValue;
    private TextView tvLowStock;

    private RecyclerView recyclerProducts;

    private FloatingActionButton fabAddProduct;







    private final ArrayList<Product> productList =
            new ArrayList<>();


    private ProductAdapter adapter;






    private DatabaseReference productsReference;






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_product
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


        fabAddProduct =
                findViewById(
                        R.id.fabAddProduct
                );






        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(this)
        );


        recyclerProducts.setHasFixedSize(false);






        adapter =
                new ProductAdapter(
                        ProductActivity.this,
                        productList
                );


        recyclerProducts.setAdapter(
                adapter
        );






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








        productsReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(currentUser.getUid());






        setupSearch();






        loadProducts();






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






    @Override
    protected void onResume() {

        super.onResume();



        if (productsReference != null) {

            loadProducts();
        }
    }






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





                        ArrayList<Product> loadedProducts =
                                new ArrayList<>();






                        int lowStockCount = 0;

                        double totalStockValue = 0.0;






                        for (DataSnapshot productSnapshot :
                                snapshot.getChildren()) {

                            Product product =
                                    createProductSafely(
                                            productSnapshot
                                    );


                            if (product == null) {
                                continue;
                            }






                            product.setProductId(
                                    productSnapshot.getKey()
                            );


                            loadedProducts.add(
                                    product
                            );






                            int quantity =
                                    product.getQuantity();






                            if (quantity > 0
                                    && quantity <= LOW_STOCK_LIMIT) {

                                lowStockCount++;
                            }













                            double costPrice =
                                    product.getCostPrice();


                            totalStockValue +=
                                    quantity * costPrice;
                        }






                        productList.clear();

                        productList.addAll(
                                loadedProducts
                        );






                        updateSummary(
                                loadedProducts.size(),
                                lowStockCount,
                                totalStockValue
                        );













                        adapter.updateList(
                                loadedProducts
                        );






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






    private void updateSummary(
            int totalProducts,
            int lowStockCount,
            double stockValue
    ) {





        tvTotalProducts.setText(
                String.valueOf(
                        totalProducts
                )
        );






        tvLowStock.setText(
                String.valueOf(
                        lowStockCount
                )
        );






        tvStockValue.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        stockValue
                )
        );
    }






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

                    }
                }
        );
    }






    private Product createProductSafely(
            DataSnapshot snapshot
    ) {

        try {





            String productName =
                    getStringValue(
                            snapshot.child("productName")
                    );






            String categoryId =
                    getStringValue(
                            snapshot.child("categoryId")
                    );






            String category =
                    getStringValue(
                            snapshot.child("category")
                    );






            String brandName =
                    getStringValue(
                            snapshot.child("brandName")
                    );






            String description =
                    getStringValue(
                            snapshot.child("description")
                    );






            String imageUrl =
                    getStringValue(
                            snapshot.child("imageUrl")
                    );






            int quantity =
                    getIntValue(
                            snapshot.child("quantity")
                    );






            double costPrice =
                    getDoubleValue(
                            snapshot.child("costPrice")
                    );






            double sellingPrice =
                    getDoubleValue(
                            snapshot.child("sellingPrice")
                    );








            Object stockValue =
                    snapshot
                            .child("stock")
                            .getValue();






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



        if (value instanceof Number) {

            return (
                    (Number) value
            ).intValue();
        }



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



        if (value instanceof Number) {

            return (
                    (Number) value
            ).doubleValue();
        }



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