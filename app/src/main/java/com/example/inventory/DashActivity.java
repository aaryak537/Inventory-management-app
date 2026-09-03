package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Locale;

public class DashActivity extends AppCompatActivity {

    private static final int LOW_STOCK_LIMIT = StockUtils.LOW_STOCK_LIMIT;

    private ImageView notify;
    private FloatingActionButton addProduct;
    private BottomNavigationView bottomNavigation;

    private LinearLayout qCategory;
    private LinearLayout qSales;
    private LinearLayout qProduct;
    private LinearLayout qSupplier;
    private LinearLayout qIncomingPurchase;
    private LinearLayout qAddSale;
    private LinearLayout qAddPurchase;
    private LinearLayout qReport;
    private LinearLayout qMovement;
    private LinearLayout qSettings, qNotify;
    private MaterialCardView cardRevenue;
    private TextView totalPro;
    private TextView low;
    private TextView userName;
    private TextView category;
    private TextView suppliers;
    private TextView revenue;
    private TextView sales;
    private TextView incomingPurchases;

    private DatabaseReference databaseReference;
    private DatabaseReference salesReference;
    private DatabaseReference suppliersReference;
    private DatabaseReference purchasesReference;

    private int totalProducts;
    private int lowStock;
    private int totalSuppliers;
    private int totalIncomingPurchases;
    private int totalQuantitySold;
    private double totalRevenue;

    private final HashSet<String> categorySet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        initializeViews();

        totalPro.setText("0");
        low.setText("0");
        category.setText("0");
        suppliers.setText("0");
        sales.setText("0");
        revenue.setText("₹0.00");
        incomingPurchases.setText("0");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("Dashboard", "No authenticated Firebase user");
            return;
        }

        String userId = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Products").child(userId);
        salesReference = FirebaseDatabase.getInstance().getReference("Sales").child(userId);
        suppliersReference = FirebaseDatabase.getInstance().getReference("Suppliers").child(userId);
        purchasesReference = FirebaseDatabase.getInstance().getReference("Purchases").child(userId);

        loadUserName(userId);
        loadDashboard();
        loadSales();
        loadSuppliers();
        loadIncomingPurchases();
        setupClickListeners();
    }

    private void initializeViews() {
        notify = findViewById(R.id.imgNotify);
        addProduct = findViewById(R.id.fabAddProduct);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        qCategory = findViewById(R.id.llCategory);
        qSales = findViewById(R.id.llSales);
        qProduct = findViewById(R.id.llProduct);
        qSupplier = findViewById(R.id.llSupplier);
        qIncomingPurchase = findViewById(R.id.llIncomingPurchase);
        qAddSale = findViewById(R.id.actionAddSale);
        qAddPurchase = findViewById(R.id.actionAddPurchase);
        qReport = findViewById(R.id.actionReport);
        qMovement = findViewById(R.id.actionMovement);
        qSettings = findViewById(R.id.actionSettings);
        qNotify = findViewById(R.id.actionNotify);
        cardRevenue = findViewById(R.id.cardRevenue);
        userName = findViewById(R.id.tvUsername);
        totalPro = findViewById(R.id.tvTotalProducts);
        low = findViewById(R.id.tvLowStock);
        category = findViewById(R.id.tvCategories);
        suppliers = findViewById(R.id.tvSuppliers);
        revenue = findViewById(R.id.tvRevenue);
        sales = findViewById(R.id.tvSales);
        incomingPurchases = findViewById(R.id.tvIncomingPurchases);
    }

    private void loadUserName(String userId) {
        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            userName.setText(String.valueOf(snapshot.getValue()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Dashboard", "Username error: " + error.getMessage());
                    }
                });
    }

    private void loadDashboard() {
        if (databaseReference == null) {
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalProducts = 0;
                lowStock = 0;
                categorySet.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    totalProducts++;
                    int quantity = getQuantity(ds);

                    if (quantity > 0 && quantity <= LOW_STOCK_LIMIT) {
                        lowStock++;
                    }

                    String categoryName = getString(ds.child("category"));
                    if (!categoryName.isEmpty()) {
                        categorySet.add(categoryName);
                    }
                }

                totalPro.setText(String.valueOf(totalProducts));
                low.setText(String.valueOf(lowStock));
                category.setText(String.valueOf(categorySet.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Product Firebase Error: " + error.getMessage());
            }
        });
    }

    private int getQuantity(DataSnapshot productSnapshot) {
        DataSnapshot quantitySnapshot = productSnapshot.child("quantity");

        if (quantitySnapshot.exists()) {
            return getIntFromSnapshot(quantitySnapshot);
        }

        DataSnapshot stockSnapshot = productSnapshot.child("stock");
        if (stockSnapshot.exists()) {
            Object stockValue = stockSnapshot.getValue();

            if (stockValue instanceof Number) {
                return ((Number) stockValue).intValue();
            }

            if (stockValue != null) {
                try {
                    return Integer.parseInt(String.valueOf(stockValue).trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }

        return 0;
    }

    private void loadSales() {
        if (salesReference == null) {
            return;
        }

        salesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalQuantitySold = 0;
                totalRevenue = 0.0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    totalQuantitySold += getIntFromSnapshot(ds.child("quantity"));
                    totalRevenue += getDoubleFromSnapshot(ds.child("totalAmount"));
                }

                sales.setText(String.valueOf(totalQuantitySold));
                revenue.setText(String.format(Locale.getDefault(), "₹%.2f", totalRevenue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Sales Firebase Error: " + error.getMessage());
            }
        });
    }

    private void loadSuppliers() {
        if (suppliersReference == null) {
            return;
        }

        suppliersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalSuppliers = (int) snapshot.getChildrenCount();
                suppliers.setText(String.valueOf(totalSuppliers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Supplier Firebase Error: " + error.getMessage());
            }
        });
    }

    private void loadIncomingPurchases() {
        if (purchasesReference == null) {
            return;
        }

        purchasesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalIncomingPurchases = (int) snapshot.getChildrenCount();
                incomingPurchases.setText(String.valueOf(totalIncomingPurchases));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Purchases Firebase Error: " + error.getMessage());
            }
        });
    }

    private void setupClickListeners() {
        addProduct.setOnClickListener(v -> open(AddProActivity.class));
        notify.setOnClickListener(v -> open(NotifyActivity.class));
        qNotify.setOnClickListener(v -> open(NotifyActivity.class));
        cardRevenue.setOnClickListener(v -> open(ReportActivity.class));
        qProduct.setOnClickListener(v -> open(ProductActivity.class));
        qSupplier.setOnClickListener(v -> open(SupplierActivity.class));
        qCategory.setOnClickListener(v -> open(CategoryActivity.class));
        qIncomingPurchase.setOnClickListener(v -> open(IncomingPurchaseActivity.class));

        qAddSale.setOnClickListener(v -> open(AddSaleActivity.class));
        qAddPurchase.setOnClickListener(v -> open(AddPurchaseActivity.class));
        qReport.setOnClickListener(v -> open(ReportActivity.class));
        qMovement.setOnClickListener(v -> open(InventoryMovementActivity.class));
        qSettings.setOnClickListener(v -> open(SettingsActivity.class));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }

            if (id == R.id.nav_products) {
                open(ProductActivity.class);
                return true;
            }

            if (id == R.id.nav_reports) {
                open(ReportActivity.class);
                return true;
            }

            if (id == R.id.nav_profile) {
                open(SettingsActivity.class);
                return true;
            }

            return false;
        });
    }

    private void open(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    private int getIntFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return 0;
        }

        Object value = snapshot.getValue();

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (Exception ignored) {
                try {
                    return (int) Double.parseDouble(String.valueOf(value).trim());
                } catch (Exception ignoredAgain) {
                    return 0;
                }
            }
        }

        return 0;
    }

    private double getDoubleFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return 0.0;
        }

        Object value = snapshot.getValue();

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (Exception ignored) {
                return 0.0;
            }
        }

        return 0.0;
    }

    private String getString(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists() || snapshot.getValue() == null) {
            return "";
        }

        return String.valueOf(snapshot.getValue()).trim();
    }
}
