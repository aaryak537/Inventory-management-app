package com.example.inventory;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditProActivity extends AppCompatActivity {

    private ImageButton back;
    private ImageView imgPro;
    private Button changeImg, update, btnDelete;
    private TextInputEditText productName, costPrice, sellingPrice, etStock, etDescription;
    private Spinner spCategory;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    private Uri imgUri;
    private String proId;
    private String imgUrl = "";

    private final String[] categories = {
            "Electronics",
            "Fashion",
            "Groceries",
            "Furniture",
            "Books",
            "Sports",
            "Others"
    };

    private final ActivityResultLauncher<String> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            imgUri = result;
                            imgPro.setImageURI(result);
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

        storageReference = FirebaseStorage.getInstance().getReference("ProductImages");
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories);

        spCategory.setAdapter(adapter);

        proId = getIntent().getStringExtra("productId");

        if (TextUtils.isEmpty(proId)) {
            Toast.makeText(this, "Invalid Product", Toast.LENGTH_SHORT).show();
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
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) return;

                        productName.setText(snapshot.child("name").getValue(String.class));
                        costPrice.setText(String.valueOf(snapshot.child("costPrice").getValue()));
                        sellingPrice.setText(String.valueOf(snapshot.child("sellingPrice").getValue()));
                        etStock.setText(String.valueOf(snapshot.child("stock").getValue()));
                        etDescription.setText(snapshot.child("description").getValue(String.class));

                        imgUrl = snapshot.child("image").getValue(String.class);

                        if (!TextUtils.isEmpty(imgUrl)) {
                            Glide.with(EditProActivity.this)
                                    .load(imgUrl)
                                    .into(imgPro);
                        }

                        String category = snapshot.child("category").getValue(String.class);

                        if (category != null) {
                            for (int i = 0; i < categories.length; i++) {
                                if (categories[i].equals(category)) {
                                    spCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(EditProActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void uploadImage() {
        if (imgUri == null) {
            updateProduct(imgUrl);
            return;
        }
        StorageReference imageRef =
                storageReference.child(System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imgUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        updateProduct(uri.toString())))
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
    private void updateProduct(String image) {

        String name = productName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String costText = costPrice.getText().toString().trim();
        String sellingText = sellingPrice.getText().toString().trim();
        String stock = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            productName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(costText)) {
            costPrice.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(sellingText)) {
            sellingPrice.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(stock)) {
            etStock.setError("Required");
            return;
        }
        double cost = Double.parseDouble(costText);
        double selling = Double.parseDouble(sellingText);

        HashMap<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("category", category);
        map.put("costPrice", cost);
        map.put("sellingPrice", selling);
        map.put("stock", stock);
        map.put("description", description);
        map.put("image", image);

        databaseReference.child(proId)
                .updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Product Updated Successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
    private void deleteProduct() {

        databaseReference.child(proId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Product Deleted",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}