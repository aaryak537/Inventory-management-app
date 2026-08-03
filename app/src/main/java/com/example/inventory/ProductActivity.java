package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity {

 EditText etSearch;
 TextView tvTotalProducts, tvStockValue;
 RecyclerView recyclerProducts;
 FloatingActionButton fabAdd;
 ArrayList<Product> productList;
 ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        fabAdd=findViewById(R.id.fabAddProduct);
        etSearch = findViewById(R.id.etSearch);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvStockValue = findViewById(R.id.tvStockValue);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        loadProducts();
        setupRecyclerView();
        updateSummary();
        searchProduct();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProductActivity.this, AddProActivity.class);
                startActivity(intent);
            }
        });
    }
    private void loadProducts() {
        productList = new ArrayList<>();

        productList.add(new Product(
                "Wireless Mouse",
                "Electronics",
                300.00,
                15,
                true));

        productList.add(new Product(
                "Keyboard",
                "Electronics",
                1399.00,
                10,
                true));

        productList.add(new Product(
                "Notebook",
                "Stationery",
                80.00,
                50,
                true));

        productList.add(new Product(
                "Printer",
                "Electronics",
                12499.00,
                2,
                false));

        productList.add(new Product(
                "Monitor",
                "Electronics",
                15499.00,
                5,
                false));
    }
    private void setupRecyclerView() {
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(adapter);
    }
    private void updateSummary() {

        int totalProducts = productList.size();
        double stockValue = 0;

        for (Product p : productList) {
            stockValue += p.getSellingPrice() * p.getQuantity();
        }
        tvTotalProducts.setText(String.valueOf(totalProducts));
        tvStockValue.setText("₹" + String.format("%.2f", stockValue));
    }
    private void searchProduct() {

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}