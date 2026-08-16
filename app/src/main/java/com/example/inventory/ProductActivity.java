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

public class ProductActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvTotalProducts, tvStockValue, tvLowStock;
    RecyclerView recyclerProducts;
    FloatingActionButton fabAdd;

    ArrayList<Product> productList;
    ProductAdapter adapter;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);



        fabAdd = findViewById(R.id.fabAddProduct);
        etSearch = findViewById(R.id.etSearch);

        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvStockValue = findViewById(R.id.tvStockValue);
        tvLowStock = findViewById(R.id.tvLowStock);

        recyclerProducts = findViewById(R.id.recyclerProducts);



        productList = new ArrayList<>();

        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(ProductActivity.this)
        );

        adapter = new ProductAdapter(
                ProductActivity.this,
                productList
        );

        recyclerProducts.setAdapter(adapter);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Products")
                    .child(user.getUid());

            loadProducts();

        } else {

            Toast.makeText(
                    ProductActivity.this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();
        }


        searchProduct();



        fabAdd.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProductActivity.this,
                    AddProActivity.class
            );

            startActivity(intent);
        });
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (databaseReference != null) {
            loadProducts();
        }
    }



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

                        double stockValue = 0;
                        int lowStockCount = 0;



                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Product product =
                                    ds.getValue(Product.class);

                            if (product != null) {

                                product.setProductId(ds.getKey());

                                productList.add(product);

                                // Stock Value
                                stockValue +=
                                        product.getCostPrice()
                                                * product.getQuantity();

                                // Low Stock
                                if (product.getQuantity() <= 10) {
                                    lowStockCount++;
                                }

                            } else {

                                Toast.makeText(
                                        ProductActivity.this,
                                        "Invalid product: "
                                                + ds.getKey(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }


                        int totalProducts =
                                productList.size();

                        tvTotalProducts.setText(
                                String.valueOf(totalProducts)
                        );

                        tvStockValue.setText(
                                "₹" + String.format(
                                        "%.2f",
                                        stockValue
                                )
                        );

                        tvLowStock.setText(
                                String.valueOf(lowStockCount)
                        );



                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ProductActivity.this,
                                "Failed to load products: "
                                        + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }



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

                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }
}