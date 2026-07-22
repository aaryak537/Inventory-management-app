package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.net.Uri;

import android.text.Editable;
import android.text.TextWatcher;

import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class AddProActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    EditText proName, costPrice, sellingPrice, stock, description;
    EditText productId, brand;
    AutoCompleteTextView autoCategory;
    ArrayAdapter<String> categoryAdapter;
    int quantity;
    ImageView back;
    Button savePro;
    DatabaseReference databaseReference;
    ImageView proImg;
    TextView stockStatus, profit, stockValue;
    Uri imageUri;

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

        savePro = findViewById(R.id.btnSaveProduct);

        back = findViewById(R.id.btnBack);

        proImg = findViewById(R.id.imgProduct);
        autoCategory = findViewById(R.id.autoCategory);

        productId = findViewById(R.id.etProductId);
        brand = findViewById(R.id.etBrand);

        stockStatus = findViewById(R.id.tvStockStatus);
        profit = findViewById(R.id.tvProfit);
        stockValue = findViewById(R.id.tvStockValue);

        String[] categories = {
                "Electronics",
                "Groceries",
                "Clothing",
                "Furniture",
                "Books",
                "Sports",
                "Beauty",
                "Home Appliances",
                "Stationery",
                "Toys"
        };

        databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null) {

                        imageUri = result.getData().getData();
                        proImg.setImageURI(imageUri);
                    }
                }
        );
        proImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        autoCategory.setAdapter(categoryAdapter);

        autoCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCategory.showDropDown();
            }
        });
        costPrice.addTextChangedListener(textWatcher);
        sellingPrice.addTextChangedListener(textWatcher);
        stock.addTextChangedListener(textWatcher);

        savePro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddProActivity.this, DashActivity.class);
                startActivity(intent);
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        imagePickerLauncher.launch(intent);
    }
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            calculateValues();
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private void calculateValues() {

        try {
            double cost = Double.parseDouble(
                    costPrice.getText().toString());

            double sell = Double.parseDouble(
                    sellingPrice.getText().toString());

            int stocks = Integer.parseInt(
                    stock.getText().toString());

            double profits = sell - cost;

            double stockVal = sell * stocks;

            profit.setText("₹ " + profits);

            stockValue.setText("₹ " + stockVal);

            if (stocks == 0)
                stockStatus.setText("Out of Stock");

            else if (stocks <= 10)
                stockStatus.setText("Low Stock");

            else
                stockStatus.setText("In Stock");
        }
        catch (Exception e) {

            profit.setText("₹0");

            stockValue.setText("₹0");

            stockStatus.setText("-");
        }
    }
    private void saveProduct() {

        String productName = proName.getText().toString().trim();

        String proId = productId.getText().toString().trim();

        String category = autoCategory.getText().toString().trim();

        String brandName = brand.getText().toString().trim();

        String cPrice = costPrice.getText().toString().trim();

        String sellPrice = sellingPrice.getText().toString().trim();

        String stocks = stock.getText().toString().trim();

        String describe = description.getText().toString().trim();

        quantity = Integer.parseInt(stocks);

        String imageUrl = "";

        if (productName.isEmpty() || proId.isEmpty() ||
                cPrice.isEmpty() ||sellPrice.isEmpty()) {

            Toast.makeText(this, "Fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Product product = new Product(
                productName,
                category,
                quantity,
                brandName,
                cPrice,
                sellPrice,
                stocks,
                describe,
                imageUrl
        );
        databaseReference.child(proId)
                .setValue(product)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Product Added Successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}