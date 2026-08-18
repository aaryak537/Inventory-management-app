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

    // ============================================================
    // UI
    // ============================================================

    ImageView notify;

    FloatingActionButton addProduct;

    BottomNavigationView bottomNavigation;

    // Quick Action / clickable views
    LinearLayout QCategory, Qsales,QProduct,QSupplier;

    // Statistics / cards
    TextView TotalPro,
            low,
            userName,
            category,
            suppliers,
            revenue,
            sales;

    // ============================================================
    // FIREBASE
    // ============================================================

    DatabaseReference databaseReference;
    DatabaseReference salesReference;
    DatabaseReference suppliersReference;

    // ============================================================
    // DASHBOARD VALUES
    // ============================================================

    int totalProducts = 0;
    int lowStock = 0;
    int totalSuppliers = 0;

    int totalQuantitySold = 0;

    double totalRevenue = 0;

    final HashSet<String> categorySet = new HashSet<>();

    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    private NotifyAdapter notifyAdapter;
    private ArrayList<NotifyModel> notifyList;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dash);

        // --------------------------------------------------------
        // INITIALIZE VIEWS
        // --------------------------------------------------------

        Qsales = findViewById(R.id.llSales);
        QCategory = findViewById(R.id.llCategory);
        QProduct = findViewById(R.id.llProduct);
        QSupplier=findViewById(R.id.llSupplier);
        userName = findViewById(R.id.tvUsername);

        addProduct = findViewById(R.id.fabAddProduct);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        suppliers = findViewById(R.id.tvSuppliers);

        revenue = findViewById(R.id.tvRevenue);

        sales = findViewById(R.id.tvSales);

        notify = findViewById(R.id.imgNotify);

        TotalPro = findViewById(R.id.tvTotalProducts);

        low = findViewById(R.id.tvLowStock);

        category = findViewById(R.id.tvCategories);


        // --------------------------------------------------------
        // NOTIFICATION ADAPTER
        // --------------------------------------------------------

        notifyList = new ArrayList<>();

        notifyAdapter = new NotifyAdapter(
                this,
                notifyList
        );


        // --------------------------------------------------------
        // FIREBASE USER
        // --------------------------------------------------------

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (user != null) {

            String userId = user.getUid();


            // ----------------------------------------------------
            // PRODUCTS
            // ----------------------------------------------------

            databaseReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference("Products")
                            .child(userId);


            // ----------------------------------------------------
            // SALES
            // ----------------------------------------------------

            salesReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference("Sales")
                            .child(userId);


            // ----------------------------------------------------
            // SUPPLIERS
            // ----------------------------------------------------

            suppliersReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference("Suppliers")
                            .child(userId);


            // ----------------------------------------------------
            // LOAD USER NAME
            // ----------------------------------------------------

            FirebaseDatabase
                    .getInstance()
                    .getReference("Users")
                    .child(userId)
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

                                    Log.e(
                                            "Dashboard",
                                            "Failed to load username: "
                                                    + error.getMessage()
                                    );
                                }
                            }
                    );
        }


        // --------------------------------------------------------
        // LOAD DASHBOARD DATA
        // --------------------------------------------------------

        loadDashboard();

        loadSales();

        loadSuppliers();


        // --------------------------------------------------------
        // NOTIFICATIONS
        // --------------------------------------------------------

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

                                Log.e(
                                        "Dashboard",
                                        "Notification error: "
                                                + error.getMessage()
                                );
                            }
                        }
                );


        // ========================================================
        // CLICK LISTENERS
        // ========================================================

        // --------------------------------------------------------
        // ADD PRODUCT
        // --------------------------------------------------------

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


        // --------------------------------------------------------
        // SALES CARD
        // --------------------------------------------------------

        Qsales.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        DashActivity.this,
                                        OutgoingSalesActivity.class
                                )
                        );
                    }
                }
        );


        // --------------------------------------------------------
        // NOTIFICATION
        // --------------------------------------------------------

        notify.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashActivity.this,
                            NotifyActivity.class
                    );

            startActivity(intent);
        });


        // --------------------------------------------------------
        // TOTAL PRODUCTS CARD
        // --------------------------------------------------------

        QProduct.setOnClickListener(
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


        // --------------------------------------------------------
        // SUPPLIERS CARD
        // --------------------------------------------------------

        QSupplier.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            DashActivity.this,
                            SupplierActivity.class
                    )
            );
        });


        // --------------------------------------------------------
        // CATEGORIES CARD
        // --------------------------------------------------------

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


        // ========================================================
        // BOTTOM NAVIGATION
        // ========================================================

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


    // ============================================================
    // LOAD PRODUCTS
    // ============================================================

    private void loadDashboard() {

        if (databaseReference == null) {

            Log.d(
                    "Dashboard",
                    "Products database reference is null"
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
                                "Product Children Count = "
                                        + snapshot.getChildrenCount()
                        );


                        totalProducts = 0;

                        lowStock = 0;

                        categorySet.clear();


                        // ------------------------------------------------
                        // READ PRODUCTS
                        // ------------------------------------------------

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


                            totalProducts++;


                            // --------------------------------------------
                            // LOW STOCK
                            // --------------------------------------------

                            if (product.getQuantity() < 10) {

                                lowStock++;
                            }


                            // --------------------------------------------
                            // CATEGORIES
                            // --------------------------------------------

                            if (product.getCategory() != null
                                    && !product.getCategory()
                                    .trim()
                                    .isEmpty()) {

                                categorySet.add(
                                        product.getCategory()
                                );
                            }
                        }


                        // ------------------------------------------------
                        // UPDATE PRODUCT STATISTICS
                        // ------------------------------------------------

                        TotalPro.setText(
                                String.valueOf(
                                        totalProducts
                                )
                        );


                        low.setText(
                                String.valueOf(
                                        lowStock
                                )
                        );


                        category.setText(
                                String.valueOf(
                                        categorySet.size()
                                )
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e(
                                "Dashboard",
                                "Product Firebase Error: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }


    // ============================================================
    // LOAD SALES
    // ============================================================

    private void loadSales() {

        if (salesReference == null) {

            Log.d(
                    "Dashboard",
                    "Sales database reference is null"
            );

            return;
        }


        salesReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        totalQuantitySold = 0;

                        totalRevenue = 0;


                        // ------------------------------------------------
                        // READ ALL SALES
                        // ------------------------------------------------

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Sale sale =
                                    ds.getValue(
                                            Sale.class
                                    );


                            if (sale == null) {
                                continue;
                            }


                            // --------------------------------------------
                            // TOTAL QUANTITY SOLD
                            // --------------------------------------------

                            totalQuantitySold +=
                                    sale.getQuantity();


                            // --------------------------------------------
                            // TOTAL REVENUE
                            // --------------------------------------------

                            totalRevenue +=
                                    sale.getTotalAmount();
                        }


                        // ------------------------------------------------
                        // UPDATE SALES CARD
                        // ------------------------------------------------

                        sales.setText(
                                String.valueOf(
                                        totalQuantitySold
                                )
                        );


                        // ------------------------------------------------
                        // UPDATE REVENUE CARD
                        // ------------------------------------------------

                        revenue.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "₹%.2f",
                                        totalRevenue
                                )
                        );


                        Log.d(
                                "Dashboard",
                                "Total Quantity Sold = "
                                        + totalQuantitySold
                        );

                        Log.d(
                                "Dashboard",
                                "Total Revenue = "
                                        + totalRevenue
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e(
                                "Dashboard",
                                "Sales Firebase Error: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }


    // ============================================================
    // LOAD SUPPLIERS
    // ============================================================

    private void loadSuppliers() {

        if (suppliersReference == null) {

            Log.d(
                    "Dashboard",
                    "Suppliers database reference is null"
            );

            return;
        }


        suppliersReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        totalSuppliers =
                                (int) snapshot.getChildrenCount();


                        suppliers.setText(
                                String.valueOf(
                                        totalSuppliers
                                )
                        );


                        Log.d(
                                "Dashboard",
                                "Total Suppliers = "
                                        + totalSuppliers
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e(
                                "Dashboard",
                                "Supplier Firebase Error: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }


    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        /*
         * Firebase ValueEventListeners are already active,
         * so the Dashboard automatically stays synchronized
         * when products, sales or suppliers change.
         */
    }
}