package com.example.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddProActivity extends AppCompatActivity {

    private EditText proName;
    private EditText costPrice;
    private EditText sellingPrice;
    private EditText stock;
    private EditText description;
    private EditText productId;
    private EditText brand;

    private AutoCompleteTextView autoCategory;

    private ImageView back;
    private ImageView proImg;

    private Button savePro;

    private TextView stockStatus;
    private TextView profit;
    private TextView stockValue;

    private DatabaseReference productsReference;
    private DatabaseReference categoriesReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Uri imageUri;

    private String selectedCategoryId = "";
    private String selectedCategoryName = "";

    private final List<String> BASIC_CATEGORIES =
            Arrays.asList(
                    "Electronics",
                    "Grocery",
                    "Clothing",
                    "Beauty & Personal Care",
                    "Home & Kitchen",
                    "Stationery",
                    "Sports",
                    "Toys",
                    "Furniture",
                    "Books",
                    "Automotive",
                    "Other"
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_addpro);

        proName = findViewById(R.id.etProductName);
        costPrice = findViewById(R.id.etCostPrice);
        sellingPrice = findViewById(R.id.etSellingPrice);
        stock = findViewById(R.id.etStock);
        description = findViewById(R.id.etDescription);
        productId = findViewById(R.id.etProductId);
        brand = findViewById(R.id.etBrand);

        autoCategory = findViewById(R.id.autoCategory);

        savePro = findViewById(R.id.btnSaveProduct);

        back = findViewById(R.id.btnBack);
        proImg = findViewById(R.id.imgProduct);

        stockStatus = findViewById(R.id.tvStockStatus);
        profit = findViewById(R.id.tvProfit);
        stockValue = findViewById(R.id.tvStockValue);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        String uid = user.getUid();

        productsReference = FirebaseDatabase.getInstance()
                .getReference("Products")
                .child(uid);

        categoriesReference = FirebaseDatabase.getInstance()
                .getReference("Categories")
                .child(uid);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(BASIC_CATEGORIES));

        autoCategory.setAdapter(categoryAdapter);
        autoCategory.setKeyListener(null);
        autoCategory.setOnClickListener(v -> autoCategory.showDropDown());

        autoCategory.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedCategoryName = parent.getItemAtPosition(position)
                            .toString();

                    selectedCategoryId = createCategoryKey(selectedCategoryName);
                }
        );

        initializeBasicCategories();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        proImg.setImageURI(imageUri);
                    }
                }
        );

        proImg.setOnClickListener(v -> openGallery());

        costPrice.addTextChangedListener(textWatcher);
        sellingPrice.addTextChangedListener(textWatcher);
        stock.addTextChangedListener(textWatcher);

        savePro.setOnClickListener(v -> saveProduct());

        back.setOnClickListener(v -> {
            Intent intent = new Intent(AddProActivity.this, DashActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void initializeBasicCategories() {

        for (String categoryName : BASIC_CATEGORIES) {
            String categoryId = createCategoryKey(categoryName);

            DatabaseReference categoryRef = categoriesReference.child(categoryId);

            categoryRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(
                                @NonNull DataSnapshot snapshot) {

                            if (!snapshot.exists()) {

                                Category category = new Category();

                                category.setId(categoryId);

                                category.setCategoryName(categoryName);

                                category.setDescription("");

                                category.setStatus("Active");

                                category.setProductCount(0);

                                categoryRef.setValue(category);
                            }
                        }

                        @Override
                        public void onCancelled(
                                @NonNull DatabaseError error) {}
                    }
            );
        }
    }

    private String createCategoryKey(
            String categoryName) {

        return categoryName
                .toLowerCase()
                .replace("&", "and")
                .replace(" ", "_")
                .replace("/", "_");
    }

    private void saveProduct() {

        String productName = proName.getText().toString().trim();

        String proId = productId.getText().toString().trim();

        String category = autoCategory.getText().toString().trim();

        String brandName = brand.getText().toString().trim();

        String stockText = stock.getText().toString().trim();

        String describe = description.getText().toString().trim();

        if (productName.isEmpty() || proId.isEmpty() || category.isEmpty()
                || costPrice.getText().toString().trim().isEmpty()
                || sellingPrice.getText().toString().trim().isEmpty()
                || stockText.isEmpty()) {
            Toast.makeText(this, "Fill all required fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!BASIC_CATEGORIES.contains(category)) {

            Toast.makeText(this, "Please select a category from the dropdown",
                    Toast.LENGTH_SHORT
            ).show();
            autoCategory.requestFocus();
            return;
        }

        double cPrice;
        double sellPrice;
        int quantity;

        try {
            cPrice = Double.parseDouble(costPrice.getText().toString().trim());

            sellPrice = Double.parseDouble(sellingPrice.getText().toString().trim());

            quantity = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid price and stock values",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String stockStatusValue;

        if (quantity <= 0) {
            stockStatusValue = "Out of Stock";
        } else if (quantity <= StockUtils.LOW_STOCK_LIMIT) {
            stockStatusValue = "Low Stock";
        } else {
            stockStatusValue = "In Stock";
        }

        String categoryId = createCategoryKey(category);

        Product product = new Product(
                productName,
                categoryId,
                category,
                quantity,
                brandName,
                cPrice,
                sellPrice,
                stockStatusValue,
                describe,
                ""
        );

        productsReference.child(proId).setValue(product)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(AddProActivity.this,
                                    "Product Added Successfully", Toast.LENGTH_SHORT
                            ).show();

                            NotifyHelper.addNotification("Product Added",
                                    productName + " added successfully"
                            );
                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(AddProActivity.this, "Failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private final TextWatcher textWatcher =
            new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateValues();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

    private void calculateValues() {

        try {
            double cost = Double.parseDouble(costPrice.getText().toString());

            double sell = Double.parseDouble(sellingPrice.getText().toString());

            int stocks = Integer.parseInt(stock.getText().toString());

            double profits = sell - cost;

            double stockVal = cost * stocks;

            profit.setText("₹ " + profits);

            stockValue.setText("₹ " + stockVal);

            if (stocks <= 0) {
                stockStatus.setText("Out of Stock");

            } else if (stocks <= StockUtils.LOW_STOCK_LIMIT) {
                stockStatus.setText("Low Stock");

            } else {
                stockStatus.setText("In Stock");
            }

        } catch (Exception e) {
            profit.setText("₹0");
            stockValue.setText("₹0");
            stockStatus.setText("-");
        }
    }
}