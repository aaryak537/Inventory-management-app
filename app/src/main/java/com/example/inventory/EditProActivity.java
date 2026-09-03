package com.example.inventory;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProActivity extends AppCompatActivity {

    private ImageButton back;
    private ImageView imgPro;
    private Button changeImg,update,btnDelete;
    private TextInputEditText productName,costPrice,sellingPrice,etStock,etDescription;
    private AutoCompleteTextView spCategory;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imgUri;
    private String imgUrl = "",proId;

    private final String[] categories = {
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

    private final ActivityResultLauncher<String> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imgUri = uri;
                            imgPro.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpro);

        back = findViewById(R.id.btnBack);
        imgPro = findViewById(R.id.imgProduct);

        changeImg = findViewById(R.id.btnChangeImage);
        update = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        productName = findViewById(R.id.etProductName);
        costPrice = findViewById(R.id.etCostPrice);
        sellingPrice = findViewById(R.id.etSellingPrice);
        etStock = findViewById(R.id.etStock);
        etDescription = findViewById(R.id.etDescription);

        spCategory = findViewById(R.id.spCategory);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                        categories);
        spCategory.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storageReference = FirebaseStorage.getInstance()
                .getReference("ProductImages")
                .child(user.getUid());

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Products")
                .child(user.getUid());

        proId = getIntent().getStringExtra("productId");

        if (TextUtils.isEmpty(proId)) {

            Toast.makeText(this, "Invalid Product",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct();

        back.setOnClickListener(v -> finish());

        changeImg.setOnClickListener(v ->
                launcher.launch("image/*"));

        update.setOnClickListener(v -> {

            if (imgUri != null) {
                uploadImage();
            } else {
                updateProduct(imgUrl);
            }
        });
        btnDelete.setOnClickListener(v -> deleteProduct());
    }

    private void loadProduct() {

        databaseReference.child(proId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {

                            Toast.makeText(EditProActivity.this,
                                    "Product not found",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        Product product = snapshot.getValue(Product.class);

                        if (product == null)
                            return;

                        productName.setText(product.getProductName());

                        costPrice.setText(String.valueOf(
                                product.getCostPrice()));

                        sellingPrice.setText(String.valueOf(
                                product.getSellingPrice()));

                        etStock.setText(String.valueOf(
                                product.getQuantity()));

                        etDescription.setText(product.getDescription());

                        imgUrl = product.getImageUrl();

                        if (imgUrl != null && !imgUrl.isEmpty()) {

                            Glide.with(EditProActivity.this)
                                    .load(imgUrl)
                                    .into(imgPro);

                        }

                        for (int i = 0; i < categories.length; i++) {

                            if (categories[i].equalsIgnoreCase(
                                    product.getCategory())) {

                                spCategory.setText(categories[i], false);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(EditProActivity.this, error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void uploadImage() {

        if (imgUri == null) {
            updateProduct(imgUrl);
            return;
        }

        StorageReference imageRef = storageReference.child(
                System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imgUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        updateProduct(uri.toString())))
                .addOnFailureListener(e ->
                        Toast.makeText(
                                EditProActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
    }

    private void updateProduct(String imageUrl) {

        String name = productName.getText().toString().trim();
        String category = spCategory.getText().toString().trim();
        String costText = costPrice.getText().toString().trim();
        String sellingText = sellingPrice.getText().toString().trim();
        String quantityText = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            productName.setError("Enter product name");
            productName.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            spCategory.setError("Select category");
            spCategory.requestFocus();
            return;
        }

        if (costText.isEmpty()) {
            costPrice.setError("Enter cost price");
            costPrice.requestFocus();
            return;
        }

        if (sellingText.isEmpty()) {
            sellingPrice.setError("Enter selling price");
            sellingPrice.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            etStock.setError("Enter quantity");
            etStock.requestFocus();
            return;
        }

        double cost = Double.parseDouble(costText);
        double selling = Double.parseDouble(sellingText);
        int quantity = Integer.parseInt(quantityText);

        String stockStatus;

        if (quantity <= 0) {
            stockStatus = "Out of Stock";
        } else if (quantity <= StockUtils.LOW_STOCK_LIMIT) {
            stockStatus = "Low Stock";
        } else {
            stockStatus = "In Stock";
        }

        Product product = new Product(
                name,
                category,
                quantity,
                "",
                cost,
                selling,
                stockStatus,
                description,
                imageUrl
        );

        databaseReference.child(proId)
                .setValue(product)
                .addOnSuccessListener(unused -> {

                    NotifyHelper.addNotification(EditProActivity.this,
                            "Product Updated",
                            name + " updated successfully");

                    Toast.makeText(EditProActivity.this,
                            "Product Updated Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EditProActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
    }
    private void deleteProduct() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    databaseReference.child(proId)
                            .removeValue()
                            .addOnSuccessListener(unused -> {

                                String name = "";

                                if (productName.getText() != null) {
                                    name = productName.getText().toString().trim();
                                }

                                NotifyHelper.addNotification(EditProActivity.this, "Product Deleted",
                                        name + " removed successfully");

                                Toast.makeText(EditProActivity.this,
                                        "Product Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(EditProActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
