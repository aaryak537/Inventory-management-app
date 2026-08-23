package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddSaleActivity extends AppCompatActivity {

    // ============================================================
    // UI
    // ============================================================

    private ImageView btnBack;

    private AutoCompleteTextView actProduct;
    private AutoCompleteTextView actPaymentMethod;

    private TextInputEditText etQuantity;
    private TextInputEditText etSellingPrice;
    private TextInputEditText etCustomerName;

    private TextInputLayout tilProduct;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilSellingPrice;

    private TextView tvAvailableStock;
    private TextView tvTotalAmount;

    private MaterialButton btnCompleteSale;


    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseAuth firebaseAuth;

    private DatabaseReference productsRef;
    private DatabaseReference salesRef;


    // ============================================================
    // PRODUCTS
    // ============================================================

    private final List<Product> productList = new ArrayList<>();
    private final List<String> productNames = new ArrayList<>();

    private ArrayAdapter<String> productAdapter;

    private Product selectedProduct;


    // ============================================================
    // PAYMENT METHODS
    // ============================================================

    private final String[] paymentMethods = {
            "Cash",
            "UPI",
            "Card",
            "Other"
    };


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addsale);

        initializeViews();
        setupFirebase();
        setupProductDropdown();
        setupPaymentDropdown();
        setupListeners();

        loadProducts();
    }


    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        actProduct = findViewById(R.id.actProduct);
        actPaymentMethod = findViewById(R.id.actPaymentMethod);

        etQuantity = findViewById(R.id.etQuantity);
        etSellingPrice = findViewById(R.id.etSellingPrice);
        etCustomerName = findViewById(R.id.etCustomerName);

        tilProduct = findViewById(R.id.tilProduct);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilSellingPrice = findViewById(R.id.tilSellingPrice);

        tvAvailableStock = findViewById(R.id.tvAvailableStock);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        btnCompleteSale = findViewById(R.id.btnCompleteSale);
    }


    // ============================================================
    // FIREBASE SETUP
    // ============================================================

    private void setupFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String userId = user.getUid();

        /*
         * YOUR EXISTING FIREBASE STRUCTURE:
         *
         * Products
         *    └── UID
         *         └── ProductID
         *
         * Sales
         *    └── UID
         *         └── SaleID
         */

        productsRef = FirebaseDatabase
                .getInstance()
                .getReference("Products")
                .child(userId);

        salesRef = FirebaseDatabase
                .getInstance()
                .getReference("Sales")
                .child(userId);
    }


    // ============================================================
    // PRODUCT DROPDOWN
    // ============================================================

    private void setupProductDropdown() {

        productAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                productNames
        );

        actProduct.setAdapter(productAdapter);
        actProduct.setThreshold(1);

        actProduct.setOnClickListener(v ->
                actProduct.showDropDown()
        );

        actProduct.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selectedName =
                            parent.getItemAtPosition(position)
                                    .toString();

                    selectProduct(selectedName);
                }
        );
    }


    // ============================================================
    // PAYMENT DROPDOWN
    // ============================================================

    private void setupPaymentDropdown() {

        ArrayAdapter<String> paymentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        paymentMethods
                );

        actPaymentMethod.setAdapter(paymentAdapter);

        actPaymentMethod.setOnClickListener(
                v -> actPaymentMethod.showDropDown()
        );
    }


    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());


        // Quantity changed
        etQuantity.addTextChangedListener(
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

                        calculateTotal();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        // Selling price changed
        etSellingPrice.addTextChangedListener(
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

                        calculateTotal();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        btnCompleteSale.setOnClickListener(
                v -> completeSale()
        );
    }


    // ============================================================
    // LOAD PRODUCTS
    // ============================================================

    private void loadProducts() {

        if (productsRef == null) {
            return;
        }

        productsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        productList.clear();
                        productNames.clear();

                        for (DataSnapshot dataSnapshot :
                                snapshot.getChildren()) {

                            Product product =
                                    dataSnapshot.getValue(
                                            Product.class
                                    );

                            if (product != null) {

                                // Firebase key = Product ID
                                product.setProductId(
                                        dataSnapshot.getKey()
                                );

                                /*
                                 * Only show products that
                                 * actually have stock.
                                 */
                                if (product.getEffectiveQuantity() > 0) {

                                    productList.add(product);

                                    String name =
                                            product.getProductName();

                                    if (name != null &&
                                            !name.trim().isEmpty()) {

                                        productNames.add(name);
                                    }
                                }
                            }
                        }

                        productAdapter.notifyDataSetChanged();

                        if (productNames.isEmpty()) {

                            Toast.makeText(
                                    AddSaleActivity.this,
                                    "No products available for sale",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        Toast.makeText(
                                AddSaleActivity.this,
                                "Failed to load products: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // SELECT PRODUCT
    // ============================================================

    private void selectProduct(String productName) {

        selectedProduct = null;

        for (Product product : productList) {

            if (product.getProductName() != null &&
                    product.getProductName()
                            .equals(productName)) {

                selectedProduct = product;
                break;
            }
        }

        if (selectedProduct == null) {

            Toast.makeText(
                    this,
                    "Product not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        clearErrors();

        // Available stock
        tvAvailableStock.setText(
                String.valueOf(
                        selectedProduct.getEffectiveQuantity()
                )
        );

        // Existing selling price
        etSellingPrice.setText(
                formatAmount(
                        selectedProduct.getSellingPrice()
                )
        );

        // Clear old quantity
        etQuantity.setText("");

        calculateTotal();
    }


    // ============================================================
    // CALCULATE TOTAL
    // ============================================================

    private void calculateTotal() {

        int quantity = getQuantity();

        double price = getSellingPrice();

        double total = quantity * price;

        tvTotalAmount.setText(
                "₹" + formatAmount(total)
        );
    }


    // ============================================================
    // GET QUANTITY
    // ============================================================

    private int getQuantity() {

        if (etQuantity.getText() == null) {
            return 0;
        }

        String value =
                etQuantity.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {
            return 0;
        }

        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException e) {

            return 0;
        }
    }


    // ============================================================
    // GET SELLING PRICE
    // ============================================================

    private double getSellingPrice() {

        if (etSellingPrice.getText() == null) {
            return 0;
        }

        String value =
                etSellingPrice.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {
            return 0;
        }

        try {

            return Double.parseDouble(value);

        } catch (NumberFormatException e) {

            return 0;
        }
    }


    // ============================================================
    // COMPLETE SALE
    // ============================================================

    private void completeSale() {

        clearErrors();


        // --------------------------------------------------------
        // PRODUCT
        // --------------------------------------------------------

        if (selectedProduct == null) {

            tilProduct.setError(
                    "Please select a product"
            );

            return;
        }


        // --------------------------------------------------------
        // QUANTITY
        // --------------------------------------------------------

        int quantity = getQuantity();

        if (quantity <= 0) {

            tilQuantity.setError(
                    "Enter a valid quantity"
            );

            return;
        }


        // --------------------------------------------------------
        // STOCK
        // --------------------------------------------------------

        int availableStock =
                selectedProduct.getEffectiveQuantity();

        if (availableStock <= 0) {

            tilQuantity.setError(
                    "Product is out of stock"
            );

            return;
        }

        if (quantity > availableStock) {

            tilQuantity.setError(
                    "Only "
                            + availableStock
                            + " available"
            );

            return;
        }


        // --------------------------------------------------------
        // SELLING PRICE
        // --------------------------------------------------------

        double sellingPrice =
                getSellingPrice();

        if (sellingPrice <= 0) {

            tilSellingPrice.setError(
                    "Enter a valid selling price"
            );

            return;
        }


        // --------------------------------------------------------
        // PAYMENT METHOD
        // --------------------------------------------------------

        String paymentMethod =
                actPaymentMethod.getText()
                        .toString()
                        .trim();

        if (paymentMethod.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select payment method",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------------
        // CUSTOMER NAME
        // --------------------------------------------------------

        String customerName = "";

        if (etCustomerName.getText() != null) {

            customerName =
                    etCustomerName.getText()
                            .toString()
                            .trim();
        }


        // --------------------------------------------------------
        // TOTAL
        // --------------------------------------------------------

        double totalAmount =
                quantity * sellingPrice;


        // --------------------------------------------------------
        // DATE & TIME
        // --------------------------------------------------------

        Date now = new Date();

        String saleDate =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                ).format(now);

        String saleTime =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(now);

        long timestamp =
                System.currentTimeMillis();


        // --------------------------------------------------------
        // SALE ID
        // --------------------------------------------------------

        String saleId =
                salesRef.push().getKey();

        if (saleId == null) {

            Toast.makeText(
                    this,
                    "Unable to create sale",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // --------------------------------------------------------
        // PRODUCT ID
        // --------------------------------------------------------

        String productId =
                selectedProduct.getProductId();

        if (productId == null ||
                productId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Product ID not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // --------------------------------------------------------
        // IMAGE
        // --------------------------------------------------------

        String imageUrl =
                selectedProduct.getImageUrl();


        // --------------------------------------------------------
        // CREATE SALE OBJECT
        // --------------------------------------------------------

        Sale sale = new Sale(
                saleId,
                productId,
                selectedProduct.getProductName(),
                imageUrl,
                quantity,
                sellingPrice,
                totalAmount,
                customerName,
                paymentMethod,
                saleDate,
                saleTime,
                timestamp
        );


        // --------------------------------------------------------
        // NEW STOCK
        // --------------------------------------------------------

        int newStock =
                availableStock - quantity;


        // --------------------------------------------------------
        // SAVE
        // --------------------------------------------------------

        saveSaleAndUpdateStock(
                sale,
                productId,
                newStock
        );
    }


    // ============================================================
    // SAVE SALE + UPDATE STOCK
    // ============================================================

    private void saveSaleAndUpdateStock(
            Sale sale,
            String productId,
            int newStock
    ) {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnCompleteSale.setEnabled(false);
        btnCompleteSale.setText("Saving Sale...");


        // ========================================================
        // PRODUCT UPDATE
        // ========================================================

        Map<String, Object> productUpdates =
                new HashMap<>();

        productUpdates.put(
                "quantity",
                newStock
        );

        productUpdates.put(
                "stock",
                StockUtils.getStockStatus(newStock)
        );


        // ========================================================
        // UPDATE PRODUCT
        // ========================================================

        productsRef
                .child(productId)
                .updateChildren(productUpdates)
                .addOnSuccessListener(unused -> {


                    // =================================================
                    // SAVE SALE
                    // =================================================

                    salesRef
                            .child(sale.getSaleId())
                            .setValue(sale)
                            .addOnSuccessListener(unused2 -> {

                                Toast.makeText(
                                        AddSaleActivity.this,
                                        "Sale completed successfully",
                                        Toast.LENGTH_SHORT
                                ).show();


                                Intent intent =
                                        new Intent(
                                                AddSaleActivity.this,
                                                OutgoingSalesActivity.class
                                        );

                                intent.addFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                                );

                                startActivity(intent);

                                finish();
                            })


                            .addOnFailureListener(e -> {

                                btnCompleteSale.setEnabled(true);

                                btnCompleteSale.setText(
                                        "Complete Sale"
                                );

                                Toast.makeText(
                                        AddSaleActivity.this,
                                        "Stock updated, but sale could not be saved: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })


                .addOnFailureListener(e -> {

                    btnCompleteSale.setEnabled(true);

                    btnCompleteSale.setText(
                            "Complete Sale"
                    );

                    Toast.makeText(
                            AddSaleActivity.this,
                            "Failed to update stock: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // ============================================================
    // CLEAR ERRORS
    // ============================================================

    private void clearErrors() {

        tilProduct.setError(null);
        tilQuantity.setError(null);
        tilSellingPrice.setError(null);
    }


    // ============================================================
    // FORMAT AMOUNT
    // ============================================================

    private String formatAmount(double amount) {

        if (amount == (long) amount) {

            return String.format(
                    Locale.getDefault(),
                    "%d",
                    (long) amount
            );

        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}