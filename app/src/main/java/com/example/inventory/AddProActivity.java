package com.example.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

public class AddProActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    EditText proName;
    EditText costPrice;
    EditText sellingPrice;
    EditText stock;
    EditText description;
    EditText productId;
    EditText brand;

    AutoCompleteTextView autoCategory;

    ArrayAdapter<String> categoryAdapter;

    // Stores Firebase category objects
    ArrayList<Category> categoryList;

    int quantity;

    ImageView back;
    ImageView proImg;

    Button savePro;

    DatabaseReference databaseReference;
    DatabaseReference categoryReference;

    TextView stockStatus;
    TextView profit;
    TextView stockValue;

    Uri imageUri;

    // Selected Firebase category
    private String selectedCategoryId = "";
    private String selectedCategoryName = "";

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



        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }



        databaseReference =
                FirebaseDatabase.getInstance()
                        .getReference("Products")
                        .child(user.getUid());


        categoryReference =
                FirebaseDatabase.getInstance()
                        .getReference("Categories")
                        .child(user.getUid();

        categoryList = new ArrayList<>();



        ArrayList<String> categoryNames =
                new ArrayList<>();

        categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        categoryNames
                );

        autoCategory.setAdapter(
                categoryAdapter
        );

        loadCategories();


        autoCategory.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position < categoryList.size()) {

                        Category selectedCategory =
                                categoryList.get(position);

                        selectedCategoryId =
                                selectedCategory.getId();

                        selectedCategoryName =
                                selectedCategory
                                        .getCategoryName();

                        Toast.makeText(
                                AddProActivity.this,
                                "Selected: "
                                        + selectedCategoryName,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );




        autoCategory.setOnClickListener(
                v -> autoCategory.showDropDown()
        );




        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .StartActivityForResult(),

                        result -> {

                            if (result.getResultCode()
                                    == RESULT_OK
                                    && result.getData()
                                    != null) {

                                imageUri =
                                        result.getData()
                                                .getData();

                                proImg.setImageURI(
                                        imageUri
                                );
                            }
                        }
                );


        proImg.setOnClickListener(
                v -> openGallery()
        );


        costPrice.addTextChangedListener(
                textWatcher
        );

        sellingPrice.addTextChangedListener(
                textWatcher
        );

        stock.addTextChangedListener(
                textWatcher
        );




        savePro.setOnClickListener(
                v -> saveProduct()
        );




        back.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AddProActivity.this,
                            DashActivity.class
                    );

            startActivity(intent);

            finish();
        });
    }


    private void loadCategories() {

        categoryReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        categoryList.clear();

                        categoryAdapter.clear();


                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Category category =
                                    ds.getValue(
                                            Category.class
                                    );

                            if (category != null) {



                                category.setId(
                                        ds.getKey()
                                );

                                categoryList.add(
                                        category
                                );

                                categoryAdapter.add(
                                        category
                                                .getCategoryName()
                                );
                            }
                        }


                        categoryAdapter.notifyDataSetChanged();


                        if (categoryList.isEmpty()) {

                            Toast.makeText(
                                    AddProActivity.this,
                                    "No categories found. Add a category first.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AddProActivity.this,
                                "Failed to load categories: "
                                        + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }



    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK
                );

        intent.setType("image/*");

        imagePickerLauncher.launch(intent);
    }




    TextWatcher textWatcher =
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

                    calculateValues();
                }

                @Override
                public void afterTextChanged(
                        Editable s) {
                }
            };


    private void calculateValues() {

        try {

            double cost =
                    Double.parseDouble(
                            costPrice
                                    .getText()
                                    .toString()
                    );

            double sell =
                    Double.parseDouble(
                            sellingPrice
                                    .getText()
                                    .toString()
                    );

            int stocks =
                    Integer.parseInt(
                            stock
                                    .getText()
                                    .toString()
                    );



            double profits =
                    sell - cost;



            double stockVal =
                    cost * stocks;


            profit.setText(
                    "₹ " + profits
            );

            stockValue.setText(
                    "₹ " + stockVal
            );


            if (stocks == 0) {

                stockStatus.setText(
                        "Out of Stock"
                );

            } else if (stocks <= 10) {

                stockStatus.setText(
                        "Low Stock"
                );

            } else {

                stockStatus.setText(
                        "In Stock"
                );
            }


        } catch (Exception e) {

            profit.setText("₹0");

            stockValue.setText("₹0");

            stockStatus.setText("-");
        }
    }



    private void saveProduct() {

        String productName =
                proName.getText()
                        .toString()
                        .trim();


        String proId =
                productId.getText()
                        .toString()
                        .trim();


        String category =
                autoCategory.getText()
                        .toString()
                        .trim();


        String brandName =
                brand.getText()
                        .toString()
                        .trim();


        String stockText =
                stock.getText()
                        .toString()
                        .trim();


        String describe =
                description.getText()
                        .toString()
                        .trim();



        if (productName.isEmpty()
                || proId.isEmpty()
                || category.isEmpty()
                || costPrice.getText()
                .toString()
                .trim()
                .isEmpty()
                || sellingPrice.getText()
                .toString()
                .trim()
                .isEmpty()
                || stockText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Fill all required fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }




        if (selectedCategoryId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select a category from the list",
                    Toast.LENGTH_SHORT
            ).show();

            autoCategory.requestFocus();

            return;
        }



        double cPrice;

        double sellPrice;

        try {

            cPrice =
                    Double.parseDouble(
                            costPrice
                                    .getText()
                                    .toString()
                                    .trim()
                    );

            sellPrice =
                    Double.parseDouble(
                            sellingPrice
                                    .getText()
                                    .toString()
                                    .trim()
                    );

            quantity =
                    Integer.parseInt(
                            stockText
                    );

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Enter valid price and stock values",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }




        String stockStatusValue;

        if (quantity <= 0) {

            stockStatusValue =
                    "Out of Stock";

        } else if (quantity <= 10) {

            stockStatusValue =
                    "Low Stock";

        } else {

            stockStatusValue =
                    "In Stock";
        }



        String imageUrl = "";




        Product product =
                new Product(
                        productName,

                        selectedCategoryId,

                        selectedCategoryName,

                        quantity,

                        brandName,

                        cPrice,

                        sellPrice,

                        stockStatusValue,

                        describe,

                        imageUrl
                );

        databaseReference
                .child(proId)
                .setValue(product)

                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Product Added Successfully",
                                    Toast.LENGTH_SHORT
                            ).show();


                            NotifyHelper.addNotification(
                                    "Product Added",
                                    productName
                                            + " added successfully"
                            );


                            finish();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }
}