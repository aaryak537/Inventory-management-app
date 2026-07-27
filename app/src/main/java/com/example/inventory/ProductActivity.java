package com.example.inventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity {

 EditText etSearch;
 TextView tvTotalProducts, tvStockValue;
 RecyclerView recyclerProducts;

 ArrayList<Product> productList;
 ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        etSearch = findViewById(R.id.etSearch);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvStockValue = findViewById(R.id.tvStockValue);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        loadProducts();
        setupRecyclerView();
        updateSummary();
        searchProduct();
    }
    private void loadProducts() {
        productList = new ArrayList<>();

        productList.add(new Product(
                "Wireless Mouse",
                "Electronics",
                999.00,
                15,
                true));

        productList.add(new Product(
                "Keyboard",
                "Electronics",
                1499.00,
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
                12500.00,
                2,
                true));

        productList.add(new Product(
                "Monitor",
                "Electronics",
                9999.00,
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