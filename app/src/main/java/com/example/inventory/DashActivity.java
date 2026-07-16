package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.HashSet;

public class DashActivity extends AppCompatActivity {

    ImageView notify;
    FloatingActionButton addProduct;
    LinearLayout addPro, QCategory, report, supplier;
    RecyclerView recent;
    TextView TotalPro, low, userName, category, revenue, sales;
    DatabaseReference databaseReference;

    int totalProducts = 0;
    int lowStock = 0;
    double totalRevenue = 0;
    HashSet<String> categorySet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash);

        userName = findViewById(R.id.tvUsername);
        category = findViewById(R.id.tvCategories);
        addProduct = findViewById(R.id.fabAddProduct);
        addPro = findViewById(R.id.llAddPro);
        QCategory = findViewById(R.id.llCategory);
        supplier = findViewById(R.id.llSupply);
        revenue = findViewById(R.id.tvRevenue);
        sales = findViewById(R.id.tvSales);
        recent = findViewById(R.id.rvRecentActivity);
        notify = findViewById(R.id.imgNotify);
        report = findViewById(R.id.llReport);

        TotalPro = findViewById(R.id.tvTotalProducts);
        low = findViewById(R.id.tvLowStock);

        databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        loadDashboard();

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DashActivity.this, AddProActivity.class);
                startActivity(intent);
            }
        });
        addPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DashActivity.this, AddProActivity.class);
                startActivity(intent);
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

                    if (product != null) {
                        totalProducts++;

                        if (product.getQuantity() < 10) {
                            lowStock++;
                        }
                        totalRevenue += product.getPrice() * product.getQuantity();

                        categorySet.add(product.getCategory());
                    }
                }
                TotalPro.setText(String.valueOf(totalProducts));

                low.setText(String.valueOf(lowStock));

                revenue.setText("₹" + totalRevenue);

                category.setText(String.valueOf(categorySet.size()));

                sales.setText(String.valueOf(totalProducts));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}