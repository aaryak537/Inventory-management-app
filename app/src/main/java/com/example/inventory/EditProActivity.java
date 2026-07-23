package com.example.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditProActivity extends AppCompatActivity {

    ImageButton back;
    ImageView imgPro;
    Button changeImg, update, btnDelete;
    TextInputEditText productName, costPrice, sellingPrice, etStock, etDescription;
    Spinner spCategory;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    Uri imgUri = null;
    String proId, imgUrl;
    String[] categories = {
            "Electronics",
            "Fashion",
            "Groceries",
            "Furniture",
            "Books",
            "Sports",
            "Others"
    };
    ActivityResultLauncher<String> launcher =
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

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Products");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);

        spCategory.setAdapter(adapter);

        proId = getIntent().getStringExtra("productId");

        if (proId != null) {
            loadProduct();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EditProActivity.this, DashActivity.class);
                startActivity(intent);
            }
        });


        changeImg.setOnClickListener(v ->
                launcher.launch("image/*"));
        // Update Product
        update.setOnClickListener(v -> {
            if (imgUri != null) {
                uploadImage();
            } else {
                updateProduct(imgUrl);
            }
        });
        // Delete Product
        btnDelete.setOnClickListener(v -> {
            databaseReference.child(proId)
                    .removeValue()
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(this,
                                "Product Deleted",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
        });
    }
  private void loadProduct() {
        databaseReference.child(proId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            productName.setText(snapshot.child("name").getValue(String.class));
                            costPrice.setText(String.valueOf(snapshot.child("costPrice").getValue()));
                            sellingPrice.setText(String.valueOf(snapshot.child("sellingPrice").getValue()));
                            etStock.setText(String.valueOf(snapshot.child("stock").getValue()));
                            etDescription.setText(snapshot.child("description").getValue(String.class));
                            imgUrl = snapshot.child("image").getValue(String.class);
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
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
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
                               .addOnSuccessListener(uri -> {
                                   String downloadUrl = uri.toString();

                                   updateProduct(downloadUrl);

                               })

              )
                .addOnFailureListener(e ->

                       Toast.makeText(
                                EditProActivity.this,
                                e.getMessage(),
                               Toast.LENGTH_SHORT
                       ).show()
                );
    }
    private void updateProduct(String image) {

        String name = productName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        double cost = Double.parseDouble(costPrice.getText().toString().trim());
        double selling = Double.parseDouble(sellingPrice.getText().toString().trim());
        String stock = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if(TextUtils.isEmpty(name)){

            productName.setError("Required");
            return;
        }
        HashMap<String,Object> map = new HashMap<>();

        map.put("name",name);
        map.put("category",category);
        map.put("costPrice",cost);
        map.put("sellingPrice",selling);
        map.put("stock",stock);
        map.put("description",description);
       map.put("image",image);

        databaseReference.child(proId)
                .updateChildren(map)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Product Updated Successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}