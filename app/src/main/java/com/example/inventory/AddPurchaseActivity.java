package com.example.inventory;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPurchaseActivity extends AppCompatActivity {

    private AutoCompleteTextView actvProduct;
    private AutoCompleteTextView actvSupplier;

    private TextInputEditText etQuantity;
    private TextInputEditText etPurchasePrice;
    private TextInputEditText etTotalAmount;
    private TextInputEditText etPurchaseDate;
    private TextInputEditText etInvoiceNumber;
    private TextInputEditText etNotes;

    private MaterialButton btnSavePurchase;
    private ImageView btnBack;

    private FirebaseAuth auth;

    private DatabaseReference productsRef;
    private DatabaseReference suppliersRef;
    private DatabaseReference purchasesRef;

    private final ArrayList<String> productNames = new ArrayList<>();
    private final ArrayList<String> productIds = new ArrayList<>();

    private ArrayAdapter<String> productAdapter;

    private String selectedProductId = "";
    private String selectedProductName = "";

    private final ArrayList<String> supplierNames = new ArrayList<>();
    private final ArrayList<String> supplierIds = new ArrayList<>();

    private ArrayAdapter<String> supplierAdapter;

    private String selectedSupplierId = "";
    private String selectedSupplierName = "";

    private int currentStock = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addpurchase);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        productsRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(uid);

        suppliersRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Suppliers")
                        .child(uid);

        purchasesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Purchases")
                        .child(uid);

        initializeViews();
        setupAdapters();
        setupListeners();

        loadProducts();
        loadSuppliers();
        setCurrentDate();
    }

    private void initializeViews() {

        actvProduct =
                findViewById(R.id.actvProduct);

        actvSupplier =
                findViewById(R.id.actvSupplier);

        etQuantity =
                findViewById(R.id.etQuantity);

        etPurchasePrice =
                findViewById(R.id.etPurchasePrice);

        etTotalAmount =
                findViewById(R.id.etTotalAmount);

        etPurchaseDate =
                findViewById(R.id.etPurchaseDate);

        etInvoiceNumber =
                findViewById(R.id.etInvoiceNumber);

        etNotes =
                findViewById(R.id.etNotes);

        btnSavePurchase =
                findViewById(R.id.btnSavePurchase);

        btnBack =
                findViewById(R.id.btnBack);
    }





    private void setupAdapters() {

        productAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        productNames
                );

        supplierAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        supplierNames
                );

        actvProduct.setAdapter(productAdapter);

        actvSupplier.setAdapter(supplierAdapter);

        actvProduct.setThreshold(0);
        actvSupplier.setThreshold(0);

        actvProduct.setOnClickListener(v -> {

            if (!productNames.isEmpty()) {
                actvProduct.showDropDown();
            }
        });

        actvSupplier.setOnClickListener(v -> {

            if (!supplierNames.isEmpty()) {
                actvSupplier.showDropDown();
            }
        });
    }

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );





        actvProduct.setOnItemClickListener(
                (parent, view, position, id) -> {



                    if (position < 0 ||
                            position >= productNames.size() ||
                            position >= productIds.size()) {

                        return;
                    }

                    selectedProductName =
                            productNames.get(position);

                    selectedProductId =
                            productIds.get(position);

                    loadCurrentProductStock(
                            selectedProductId
                    );
                }
        );





        actvSupplier.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position < 0 ||
                            position >= supplierNames.size() ||
                            position >= supplierIds.size()) {

                        return;
                    }

                    selectedSupplierName =
                            supplierNames.get(position);

                    selectedSupplierId =
                            supplierIds.get(position);
                }
        );





        TextWatcher totalWatcher =
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

                        calculateTotal();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                };

        etQuantity.addTextChangedListener(
                totalWatcher
        );

        etPurchasePrice.addTextChangedListener(
                totalWatcher
        );





        etPurchaseDate.setOnClickListener(
                v -> showDatePicker()
        );





        btnSavePurchase.setOnClickListener(
                v -> savePurchase()
        );
    }





    private void loadProducts() {

        productsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        productNames.clear();
                        productIds.clear();

                        for (DataSnapshot productSnapshot :
                                snapshot.getChildren()) {

                            String productId =
                                    productSnapshot.getKey();

                            String productName =
                                    productSnapshot
                                            .child("productName")
                                            .getValue(String.class);

                            if (productId == null ||
                                    productName == null ||
                                    productName.trim().isEmpty()) {

                                continue;
                            }

                            productIds.add(productId);
                            productNames.add(productName);
                        }

                        productAdapter.notifyDataSetChanged();

                        if (productNames.isEmpty()) {

                            Toast.makeText(
                                    AddPurchaseActivity.this,
                                    "No products found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AddPurchaseActivity.this,
                                "Failed to load products: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }





    private void loadSuppliers() {

        suppliersRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        supplierNames.clear();
                        supplierIds.clear();

                        for (DataSnapshot supplierSnapshot :
                                snapshot.getChildren()) {

                            String supplierId =
                                    supplierSnapshot.getKey();



                            String supplierName =
                                    supplierSnapshot
                                            .child("name")
                                            .getValue(String.class);

                            if (supplierName == null ||
                                    supplierName.trim().isEmpty()) {

                                continue;
                            }

                            if (supplierId == null) {
                                continue;
                            }

                            supplierIds.add(
                                    supplierId
                            );

                            supplierNames.add(
                                    supplierName
                            );
                        }

                        supplierAdapter.notifyDataSetChanged();

                        if (supplierNames.isEmpty()) {

                            Toast.makeText(
                                    AddPurchaseActivity.this,
                                    "No suppliers found. Add a supplier first.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AddPurchaseActivity.this,
                                "Failed to load suppliers: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }





    private void loadCurrentProductStock(
            String productId) {

        if (productId == null ||
                productId.trim().isEmpty()) {

            currentStock = 0;
            return;
        }

        productsRef
                .child(productId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                currentStock = 0;

                                if (!snapshot.exists()) {
                                    return;
                                }



                                if (snapshot.hasChild("quantity")) {

                                    currentStock =
                                            getIntValue(
                                                    snapshot.child(
                                                            "quantity"
                                                    )
                                            );

                                } else if (snapshot.hasChild("stock")) {


                                    currentStock =
                                            getIntValue(
                                                    snapshot.child(
                                                            "stock"
                                                    )
                                            );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                currentStock = 0;

                                Toast.makeText(
                                        AddPurchaseActivity.this,
                                        "Unable to load current stock",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }





    private int getIntValue(
            DataSnapshot snapshot) {

        Object value =
                snapshot.getValue();

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Double) {
            return ((Double) value).intValue();
        }

        if (value instanceof Float) {
            return ((Float) value).intValue();
        }

        try {

            if (value != null) {

                return Integer.parseInt(
                        value.toString()
                );
            }

        } catch (Exception ignored) {
        }

        return 0;
    }





    private void calculateTotal() {

        String quantityText =
                etQuantity.getText() == null
                        ? ""
                        : etQuantity
                          .getText()
                          .toString()
                          .trim();

        String priceText =
                etPurchasePrice.getText() == null
                        ? ""
                        : etPurchasePrice
                          .getText()
                          .toString()
                          .trim();

        if (quantityText.isEmpty() ||
                priceText.isEmpty()) {

            etTotalAmount.setText("");
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(quantityText);

            double price =
                    Double.parseDouble(priceText);

            double total =
                    quantity * price;

            etTotalAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.2f",
                            total
                    )
            );

        } catch (Exception e) {

            etTotalAmount.setText("");
        }
    }





    private void setCurrentDate() {

        Calendar calendar =
                Calendar.getInstance();

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        etPurchaseDate.setText(
                format.format(
                        calendar.getTime()
                )
        );
    }





    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            etPurchaseDate.setText(
                                    date
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }





    private void savePurchase() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }





        if (selectedProductId.isEmpty()) {

            actvProduct.setError(
                    "Select a product"
            );

            actvProduct.requestFocus();

            return;
        }





        if (selectedSupplierId.isEmpty()) {

            actvSupplier.setError(
                    "Select a supplier"
            );

            actvSupplier.requestFocus();

            return;
        }

        String quantityText =
                etQuantity.getText() == null
                        ? ""
                        : etQuantity
                          .getText()
                          .toString()
                          .trim();

        String priceText =
                etPurchasePrice.getText() == null
                        ? ""
                        : etPurchasePrice
                          .getText()
                          .toString()
                          .trim();

        if (quantityText.isEmpty()) {

            etQuantity.setError(
                    "Enter purchase quantity"
            );

            etQuantity.requestFocus();

            return;
        }

        if (priceText.isEmpty()) {

            etPurchasePrice.setError(
                    "Enter purchase price"
            );

            etPurchasePrice.requestFocus();

            return;
        }

        int quantity;
        double purchasePrice;

        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

            purchasePrice =
                    Double.parseDouble(
                            priceText
                    );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Enter valid quantity and price",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (quantity <= 0) {

            etQuantity.setError(
                    "Quantity must be greater than 0"
            );

            return;
        }

        if (purchasePrice < 0) {

            etPurchasePrice.setError(
                    "Enter a valid price"
            );

            return;
        }

        double totalAmount =
                quantity * purchasePrice;

        String purchaseDate =
                etPurchaseDate.getText() == null
                        ? ""
                        : etPurchaseDate
                          .getText()
                          .toString()
                          .trim();

        String invoiceNumber =
                etInvoiceNumber.getText() == null
                        ? ""
                        : etInvoiceNumber
                          .getText()
                          .toString()
                          .trim();

        String notes =
                etNotes.getText() == null
                        ? ""
                        : etNotes
                          .getText()
                          .toString()
                          .trim();





        String purchaseId =
                purchasesRef
                        .push()
                        .getKey();

        if (purchaseId == null) {

            Toast.makeText(
                    this,
                    "Unable to create purchase ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSavePurchase.setEnabled(false);
        btnSavePurchase.setText("Saving...");





        Purchase purchase =
                new Purchase(
                        purchaseId,
                        selectedProductId,
                        selectedProductName,
                        selectedSupplierId,
                        selectedSupplierName,
                        quantity,
                        purchasePrice,
                        totalAmount,
                        purchaseDate,
                        invoiceNumber,
                        notes
                );





        int newStock =
                currentStock + quantity;

        Map<String, Object> stockUpdates =
                new HashMap<>();

        stockUpdates.put(
                "stock",
                StockUtils.getStockStatus(newStock)
        );

        stockUpdates.put(
                "quantity",
                newStock
        );

        productsRef
                .child(selectedProductId)
                .updateChildren(
                        stockUpdates
                )
                .addOnSuccessListener(
                        unused -> {


                            purchasesRef
                                    .child(purchaseId)
                                    .setValue(purchase)
                                    .addOnSuccessListener(
                                            unused2 -> {

                                                Toast.makeText(
                                                        AddPurchaseActivity.this,
                                                        "Purchase added successfully",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                finish();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {


                                                Map<String, Object>
                                                        revert =
                                                        new HashMap<>();

                                                revert.put(
                                                        "stock",
                                                        StockUtils.getStockStatus(currentStock)
                                                );

                                                revert.put(
                                                        "quantity",
                                                        currentStock
                                                );

                                                productsRef
                                                        .child(
                                                                selectedProductId
                                                        )
                                                        .updateChildren(
                                                                revert
                                                        );

                                                enableSaveButton();

                                                Toast.makeText(
                                                        AddPurchaseActivity.this,
                                                        "Purchase save failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            enableSaveButton();

                            Toast.makeText(
                                    AddPurchaseActivity.this,
                                    "Stock update failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void enableSaveButton() {

        btnSavePurchase.setEnabled(true);

        btnSavePurchase.setText(
                "Save Purchase"
        );
    }
}