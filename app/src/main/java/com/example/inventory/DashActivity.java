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
    // CONSTANTS
    // ============================================================

    /*
     * Products with quantity <= 10 are considered Low Stock.
     */
    private static final int LOW_STOCK_LIMIT = 10;


    // ============================================================
    // UI
    // ============================================================

    private ImageView notify;

    private FloatingActionButton addProduct;

    private BottomNavigationView bottomNavigation;


    // Dashboard clickable cards
    private LinearLayout QCategory;
    private LinearLayout Qsales;
    private LinearLayout QProduct;
    private LinearLayout QSupplier;
    private LinearLayout QIncomingPurchase;


    // Dashboard statistics
    private TextView TotalPro;
    private TextView low;
    private TextView userName;
    private TextView category;
    private TextView suppliers;
    private TextView revenue;
    private TextView sales;
    private TextView incomingPurchases;


    // ============================================================
    // FIREBASE
    // ============================================================

    private DatabaseReference databaseReference;
    private DatabaseReference salesReference;
    private DatabaseReference suppliersReference;
    private DatabaseReference purchasesReference;


    // ============================================================
    // DASHBOARD VALUES
    // ============================================================

    private int totalProducts = 0;

    private int lowStock = 0;

    private int totalSuppliers = 0;

    private int totalIncomingPurchases = 0;

    private int totalQuantitySold = 0;

    private double totalRevenue = 0.0;


    private final HashSet<String> categorySet =
            new HashSet<>();


    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    private NotifyAdapter notifyAdapter;

    private ArrayList<NotifyModel> notifyList;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_dash
        );


        // ========================================================
        // INITIALIZE VIEWS
        // ========================================================

        Qsales =
                findViewById(
                        R.id.llSales
                );


        QCategory =
                findViewById(
                        R.id.llCategory
                );


        QProduct =
                findViewById(
                        R.id.llProduct
                );


        QSupplier =
                findViewById(
                        R.id.llSupplier
                );


        QIncomingPurchase =
                findViewById(
                        R.id.llIncomingPurchase
                );


        userName =
                findViewById(
                        R.id.tvUsername
                );


        addProduct =
                findViewById(
                        R.id.fabAddProduct
                );


        bottomNavigation =
                findViewById(
                        R.id.bottomNavigation
                );


        suppliers =
                findViewById(
                        R.id.tvSuppliers
                );


        revenue =
                findViewById(
                        R.id.tvRevenue
                );


        sales =
                findViewById(
                        R.id.tvSales
                );


        incomingPurchases =
                findViewById(
                        R.id.tvIncomingPurchases
                );


        notify =
                findViewById(
                        R.id.imgNotify
                );


        TotalPro =
                findViewById(
                        R.id.tvTotalProducts
                );


        low =
                findViewById(
                        R.id.tvLowStock
                );


        category =
                findViewById(
                        R.id.tvCategories
                );


        // ========================================================
        // INITIAL VALUES
        // ========================================================

        TotalPro.setText("0");

        low.setText("0");

        category.setText("0");

        suppliers.setText("0");

        sales.setText("0");

        revenue.setText("₹0.00");

        incomingPurchases.setText("0");


        // ========================================================
        // NOTIFICATION ADAPTER
        // ========================================================

        notifyList =
                new ArrayList<>();


        notifyAdapter =
                new NotifyAdapter(
                        this,
                        notifyList
                );


        // ========================================================
        // FIREBASE USER
        // ========================================================

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (user == null) {

            Log.e(
                    "Dashboard",
                    "No authenticated Firebase user"
            );

            return;
        }


        String userId =
                user.getUid();


        // ========================================================
        // PRODUCTS
        // ========================================================

        databaseReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(userId);


        // ========================================================
        // SALES
        // ========================================================

        salesReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Sales")
                        .child(userId);


        // ========================================================
        // SUPPLIERS
        // ========================================================

        suppliersReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Suppliers")
                        .child(userId);


        // ========================================================
        // PURCHASES
        // ========================================================

        purchasesReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Purchases")
                        .child(userId);


        // ========================================================
        // LOAD USER NAME
        // ========================================================

        loadUserName(userId);


        // ========================================================
        // LOAD DASHBOARD
        // ========================================================

        loadDashboard();

        loadSales();

        loadSuppliers();

        loadIncomingPurchases();


        // ========================================================
        // NOTIFICATIONS
        // ========================================================

        loadNotifications();


        // ========================================================
        // BUTTONS
        // ========================================================

        setupClickListeners();
    }


    // ============================================================
    // LOAD USER NAME
    // ============================================================

    private void loadUserName(
            String userId
    ) {

        FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(userId)
                .child("name")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                if (!snapshot.exists()) {
                                    return;
                                }


                                Object value =
                                        snapshot.getValue();


                                if (value != null) {

                                    userName.setText(
                                            String.valueOf(
                                                    value
                                            )
                                    );
                                }
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                Log.e(
                                        "Dashboard",
                                        "Username error: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    // ============================================================
    // LOAD DASHBOARD PRODUCT DATA
    // ============================================================

    private void loadDashboard() {

        if (databaseReference == null) {

            Log.e(
                    "Dashboard",
                    "Products reference is null"
            );

            return;
        }


        databaseReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        // ==========================================
                        // RESET
                        // ==========================================

                        totalProducts = 0;

                        lowStock = 0;

                        categorySet.clear();


                        // ==========================================
                        // READ PRODUCTS
                        // ==========================================

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {


                            /*
                             * Count product.
                             */
                            totalProducts++;


                            /*
                             * Read quantity.
                             */
                            int quantity =
                                    getQuantity(
                                            ds
                                    );


                            /*
                             * LOW STOCK
                             *
                             * 1 - 10 = Low Stock
                             *
                             * 0 = Out of Stock
                             *
                             * We count only positive quantities
                             * as Low Stock.
                             */

                            if (quantity > 0 &&
                                    quantity <= LOW_STOCK_LIMIT) {

                                lowStock++;
                            }


                            /*
                             * Category.
                             */
                            String categoryName =
                                    getString(
                                            ds.child(
                                                    "category"
                                            )
                                    );


                            if (!categoryName.isEmpty()) {

                                categorySet.add(
                                        categoryName
                                );
                            }
                        }


                        // ==========================================
                        // UPDATE UI
                        // ==========================================

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


                        Log.d(
                                "Dashboard",
                                "Products = "
                                        + totalProducts
                                        + ", Low Stock = "
                                        + lowStock
                                        + ", Categories = "
                                        + categorySet.size()
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

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
    // GET QUANTITY
    //
    // Handles:
    // quantity = 10
    // quantity = "10"
    //
    // Also supports old records where stock may contain
    // a numeric value.
    // ============================================================

    private int getQuantity(
            DataSnapshot productSnapshot
    ) {

        DataSnapshot quantitySnapshot =
                productSnapshot.child(
                        "quantity"
                );


        if (quantitySnapshot.exists()) {

            return getIntFromSnapshot(
                    quantitySnapshot
            );
        }


        /*
         * Fallback for old Firebase records.
         */
        DataSnapshot stockSnapshot =
                productSnapshot.child(
                        "stock"
                );


        if (stockSnapshot.exists()) {

            Object stockValue =
                    stockSnapshot.getValue();


            /*
             * Numeric stock.
             */
            if (stockValue instanceof Number) {

                return ((Number) stockValue)
                        .intValue();
            }


            /*
             * String stock.
             */
            if (stockValue != null) {

                String stock =
                        String.valueOf(
                                stockValue
                        ).trim();


                try {

                    return Integer.parseInt(
                            stock
                    );

                } catch (NumberFormatException ignored) {

                    /*
                     * Text values such as:
                     *
                     * In Stock
                     * Low Stock
                     * Out of Stock
                     *
                     * cannot give an exact quantity.
                     */
                }
            }
        }


        return 0;
    }


    // ============================================================
    // LOAD SALES
    // ============================================================

    private void loadSales() {

        if (salesReference == null) {
            return;
        }


        salesReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        totalQuantitySold = 0;

                        totalRevenue = 0.0;


                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            int quantity =
                                    getIntFromSnapshot(
                                            ds.child(
                                                    "quantity"
                                            )
                                    );


                            double amount =
                                    getDoubleFromSnapshot(
                                            ds.child(
                                                    "totalAmount"
                                            )
                                    );


                            totalQuantitySold +=
                                    quantity;


                            totalRevenue +=
                                    amount;
                        }


                        sales.setText(
                                String.valueOf(
                                        totalQuantitySold
                                )
                        );


                        revenue.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "₹%.2f",
                                        totalRevenue
                                )
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

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
            return;
        }


        suppliersReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        totalSuppliers =
                                (int)
                                        snapshot
                                                .getChildrenCount();


                        suppliers.setText(
                                String.valueOf(
                                        totalSuppliers
                                )
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

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
    // LOAD INCOMING PURCHASES
    // ============================================================

    private void loadIncomingPurchases() {

        if (purchasesReference == null) {
            return;
        }


        purchasesReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        totalIncomingPurchases =
                                (int)
                                        snapshot
                                                .getChildrenCount();


                        incomingPurchases.setText(
                                String.valueOf(
                                        totalIncomingPurchases
                                )
                        );
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        Log.e(
                                "Dashboard",
                                "Purchases Firebase Error: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }


    // ============================================================
    // LOAD NOTIFICATIONS
    // ============================================================

    private void loadNotifications() {

        DatabaseReference notificationRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference(
                                "Notifications"
                        );


        notificationRef
                .limitToLast(5)
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                notifyList.clear();


                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    try {

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

                                    } catch (Exception e) {

                                        Log.e(
                                                "Dashboard",
                                                "Notification parse error",
                                                e
                                        );
                                    }
                                }


                                notifyAdapter
                                        .notifyDataSetChanged();
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                Log.e(
                                        "Dashboard",
                                        "Notification Firebase Error: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    // ============================================================
    // CLICK LISTENERS
    // ============================================================

    private void setupClickListeners() {


        // ========================================================
        // ADD PRODUCT
        // ========================================================

        addProduct.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    AddProActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // SALES
        // ========================================================

        Qsales.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    OutgoingSalesActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // PURCHASES
        // ========================================================

        QIncomingPurchase.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    IncomingPurchaseActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // PRODUCTS
        // ========================================================

        QProduct.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    ProductActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // SUPPLIERS
        // ========================================================

        QSupplier.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    SupplierActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // CATEGORIES
        // ========================================================

        QCategory.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    CategoryActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // NOTIFICATIONS
        // ========================================================

        notify.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    DashActivity.this,
                                    NotifyActivity.class
                            )
                    );
                }
        );


        // ========================================================
        // BOTTOM NAVIGATION
        // ========================================================

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();


                    if (id == R.id.nav_home) {

                        return true;
                    }


                    if (id == R.id.nav_products) {

                        startActivity(
                                new Intent(
                                        this,
                                        ProductActivity.class
                                )
                        );

                        return true;
                    }


                    if (id == R.id.nav_reports) {

                        startActivity(
                                new Intent(
                                        this,
                                        ReportActivity.class
                                )
                        );

                        return true;
                    }


                    if (id == R.id.nav_profile) {

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
    // SAFE INTEGER READER
    // ============================================================

    private int getIntFromSnapshot(
            DataSnapshot snapshot
    ) {

        if (snapshot == null ||
                !snapshot.exists()) {

            return 0;
        }


        Object value =
                snapshot.getValue();


        if (value instanceof Number) {

            return ((Number) value).intValue();
        }


        if (value != null) {

            try {

                return Integer.parseInt(
                        String.valueOf(
                                value
                        ).trim()
                );

            } catch (Exception ignored) {

                try {

                    return (int)
                            Double.parseDouble(
                                    String.valueOf(
                                            value
                                    ).trim()
                            );

                } catch (Exception ignoredAgain) {
                    return 0;
                }
            }
        }


        return 0;
    }


    // ============================================================
    // SAFE DOUBLE READER
    // ============================================================

    private double getDoubleFromSnapshot(
            DataSnapshot snapshot
    ) {

        if (snapshot == null ||
                !snapshot.exists()) {

            return 0.0;
        }


        Object value =
                snapshot.getValue();


        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }


        if (value != null) {

            try {

                return Double.parseDouble(
                        String.valueOf(
                                value
                        ).trim()
                );

            } catch (Exception ignored) {

                return 0.0;
            }
        }


        return 0.0;
    }


    // ============================================================
    // SAFE STRING READER
    // ============================================================

    private String getString(
            DataSnapshot snapshot
    ) {

        if (snapshot == null ||
                !snapshot.exists()) {

            return "";
        }


        Object value =
                snapshot.getValue();


        if (value == null) {
            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * Firebase listeners remain active.
         *
         * Dashboard values automatically update whenever
         * Products, Sales, Suppliers or Purchases change.
         */
    }
}