package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class DashActivity extends AppCompatActivity {

    ImageView notify, forward;

    FloatingActionButton addProduct;

    BottomNavigationView bottomNavigation;

    LinearLayout Pro, QCategory, supplier;

    TextView TotalPro,
            low,
            userName,
            category,
            revenue,
            sales;

    DatabaseReference databaseReference;

    int totalProducts;
    int lowStock;

    // This now represents STOCK VALUE
    double totalStockValue;

    private NotifyAdapter notifyAdapter;
    private ArrayList<NotifyModel> notifyList;

    final HashSet<String> categorySet = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dash);


        // =========================================================
        // FIND VIEWS
        // =========================================================

        forward = findViewById(R.id.forwardBtn);

        userName = findViewById(R.id.tvUsername);

        category = findViewById(R.id.tvCategories);

        addProduct = findViewById(R.id.fabAddProduct);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        Pro = findViewById(R.id.llProduct);

        QCategory = findViewById(R.id.llCategory);

        supplier = findViewById(R.id.llSupply);

        revenue = findViewById(R.id.tvRevenue);

        sales = findViewById(R.id.tvSales);

        notify = findViewById(R.id.imgNotify);

        TotalPro = findViewById(R.id.tvTotalProducts);

        low = findViewById(R.id.tvLowStock);


        // =========================================================
        // NOTIFICATION SETUP
        // =========================================================

        notifyList = new ArrayList<>();

        notifyAdapter = new NotifyAdapter(
                this,
                notifyList
        );


        // =========================================================
        // FIREBASE USER
        // =========================================================

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {

            // -----------------------------------------------------
            // Products path
            // Products
            //      └── User UID
            //              └── Product ID
            // -----------------------------------------------------

            databaseReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference("Products")
                            .child(user.getUid());


            // -----------------------------------------------------
            // Get user name
            // -----------------------------------------------------

            FirebaseDatabase
                    .getInstance()
                    .getReference("Users")
                    .child(user.getUid())
                    .child("name")
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(
                                        @NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()) {

                                        String name =
                                                snapshot.getValue(
                                                        String.class
                                                );

                                        if (name != null) {

                                            userName.setText(name);
                                        }
                                    }
                                }


                                @Override
                                public void onCancelled(
                                        @NonNull DatabaseError error) {

                                }
                            }
                    );
        }


        // =========================================================
        // LOAD DASHBOARD
        // =========================================================

        loadDashboard();


        // =========================================================
        // LOAD NOTIFICATIONS
        // =========================================================

        DatabaseReference notificationRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Notifications");


        notificationRef
                .limitToLast(5)
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                notifyList.clear();


                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    NotifyModel model =
                                            ds.getValue(
                                                    NotifyModel.class
                                            );


                                    if (model != null) {

                                        notifyList.add(
                                                0,
                                                model
                                        );
                                    }
                                }


                                notifyAdapter.notifyDataSetChanged();
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        }
                );


        // =========================================================
        // ADD PRODUCT BUTTON
        // =========================================================

        addProduct.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        AddProActivity.class
                                )
                        );
                    }
                }
        );


        // =========================================================
        // NOTIFICATION BUTTON
        // =========================================================

        notify.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashActivity.this,
                            NotifyActivity.class
                    );

            startActivity(intent);
        });


        // =========================================================
        // FORWARD BUTTON
        // =========================================================

        forward.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        ProductActivity.class
                                )
                        );
                    }
                }
        );


        // =========================================================
        // PRODUCT CARD
        // =========================================================

        Pro.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        ProductActivity.class
                                )
                        );
                    }
                }
        );


        // =========================================================
        // SUPPLIER CARD
        // =========================================================

        supplier.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        SupplierActivity.class
                                )
                        );
                    }
                }
        );


        // =========================================================
        // CATEGORY CARD
        // =========================================================

        QCategory.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        CategoryActivity.class
                                )
                        );
                    }
                }
        );
    }


    // =============================================================
    // LOAD DASHBOARD DATA
    // =============================================================

    private void loadDashboard() {


        if (databaseReference == null) {

            Log.d(
                    "Dashboard",
                    "Database reference is null"
            );

            return;
        }


        databaseReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {


                        Log.d(
                                "Dashboard",
                                "Children Count = "
                                        + snapshot.getChildrenCount()
                        );


                        // -------------------------------------------------
                        // RESET VALUES
                        // -------------------------------------------------

                        totalProducts = 0;

                        lowStock = 0;

                        totalStockValue = 0;

                        categorySet.clear();


                        // -------------------------------------------------
                        // READ PRODUCTS
                        // -------------------------------------------------

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {


                            Product product =
                                    ds.getValue(
                                            Product.class
                                    );


                            if (product == null) {

                                Log.d(
                                        "Dashboard",
                                        "Product is NULL"
                                );

                                continue;
                            }


                            Log.d(
                                    "Dashboard",
                                    "Product: "
                                            + product.getProductName()
                            );


                            // -------------------------------------------------
                            // TOTAL PRODUCTS
                            // -------------------------------------------------

                            totalProducts++;


                            // -------------------------------------------------
                            // LOW STOCK
                            // -------------------------------------------------

                            if (product.getQuantity() < 10) {

                                lowStock++;
                            }


                            // -------------------------------------------------
                            // STOCK VALUE
                            //
                            // OLD:
                            //
                            // Selling Price × Quantity
                            //
                            // NEW:
                            //
                            // Cost Price × Quantity
                            // -------------------------------------------------

                            totalStockValue +=
                                    product.getCostPrice()
                                            * product.getQuantity();


                            // -------------------------------------------------
                            // CATEGORIES
                            // -------------------------------------------------

                            if (product.getCategory() != null
                                    && !product.getCategory()
                                    .trim()
                                    .isEmpty()) {

                                categorySet.add(
                                        product.getCategory()
                                );
                            }
                        }


                        // =================================================
                        // UPDATE TOTAL PRODUCTS
                        // =================================================

                        TotalPro.setText(
                                String.valueOf(
                                        totalProducts
                                )
                        );


                        // =================================================
                        // UPDATE LOW STOCK
                        // =================================================

                        low.setText(
                                String.valueOf(
                                        lowStock
                                )
                        );


                        // =================================================
                        // UPDATE CATEGORY COUNT
                        // =================================================

                        category.setText(
                                String.valueOf(
                                        categorySet.size()
                                )
                        );


                        // =================================================
                        // UPDATE STOCK VALUE
                        //
                        // Cost Price × Quantity
                        // =================================================

                        revenue.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "₹ %.2f",
                                        totalStockValue
                                )
                        );


                        // =================================================
                        // SALES
                        //
                        // Keeping your existing logic
                        // =================================================

                        sales.setText(
                                String.valueOf(
                                        totalProducts
                                )
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e(
                                "Dashboard",
                                "Firebase Error: "
                                        + error.getMessage()
                        );
                    }
                }
        );


        // =============================================================
        // BOTTOM NAVIGATION
        // =============================================================

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id = item.getItemId();


                    if (id == R.id.nav_home) {

                        return true;
                    }


                    else if (id == R.id.nav_products) {

                        startActivity(
                                new Intent(
                                        this,
                                        ProductActivity.class
                                )
                        );

                        return true;
                    }


                    else if (id == R.id.nav_reports) {

                        startActivity(
                                new Intent(
                                        this,
                                        ReportActivity.class
                                )
                        );

                        return true;
                    }


                    else if (id == R.id.nav_profile) {

                        startActivity(
                                new Intent(
                                        this,
                                        SettingsActivity.class
                                )
                        );

                        return true;
                    }
                    return false;
                }
        );
    }
}