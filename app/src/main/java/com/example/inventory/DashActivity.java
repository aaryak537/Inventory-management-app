package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.HashSet;
import java.util.Locale;

public class DashActivity extends AppCompatActivity {
     ImageView notify;
     FloatingActionButton addProduct;

     LinearLayout addPro, QCategory, report, supplier;

     RecyclerView recent;

     TextView TotalPro, low, userName, category, revenue, sales;

     DatabaseReference databaseReference;
     int totalProducts;
     int lowStock;
     double totalRevenue;

     final HashSet<String> categorySet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);


        userName = findViewById(R.id.tvUsername);
        category = findViewById(R.id.tvCategories);

        addProduct = findViewById(R.id.fabAddProduct);

        addPro = findViewById(R.id.llAddPro);
        QCategory = findViewById(R.id.llCategory);
        supplier = findViewById(R.id.llSupply);
        report = findViewById(R.id.llReport);

        revenue = findViewById(R.id.tvRevenue);
        sales = findViewById(R.id.tvSales);

        recent = findViewById(R.id.rvRecentActivity);

        notify = findViewById(R.id.imgNotify);

        TotalPro = findViewById(R.id.tvTotalProducts);
        low = findViewById(R.id.tvLowStock);

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Products");

        loadDashboard();

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashActivity.this,
                        AddProActivity.class));
            }
        });

        addPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashActivity.this,
                        AddProActivity.class));
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashActivity.this,
                        ReportActivity.class));
            }
        });
    }
    private void loadDashboard() {

        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                totalProducts = 0;
                lowStock = 0;
                totalRevenue = 0;
                categorySet.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Product product = ds.getValue(Product.class);

                    if (product == null) {
                        continue;
                    }
                    totalProducts++;

                    if (product.getQuantity() < 10) {
                        lowStock++;
                    }
                    totalRevenue +=
                            product.getSellingPrice()*product.getQuantity();
                    if (product.getCategory() != null) {
                        categorySet.add(product.getCategory());
                    }
                }

                TotalPro.setText(String.valueOf(totalProducts));
                low.setText(String.valueOf(lowStock));
                sales.setText(String.valueOf(totalProducts));

                revenue.setText(String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        totalRevenue));

                category.setText(String.valueOf(categorySet.size()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}