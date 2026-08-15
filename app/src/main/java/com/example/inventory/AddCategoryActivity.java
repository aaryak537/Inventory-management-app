package com.example.inventory;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etCategoryName, etCategoryDescription;
    private Spinner spStatus;
    private Button btnSaveCategory;

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcategory);

        btnBack = findViewById(R.id.btnBack);
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        spStatus = findViewById(R.id.spStatus);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Categories")
                .child(user.getUid());


        // Spinner Items
        String[] status = {
                "Active",
                "Inactive"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                status
        );

        spStatus.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {

        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();
        String status = spStatus.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etCategoryName.setError("Enter category name");
            etCategoryName.requestFocus();
            return;
        }

        progressDialog.setMessage("Saving Category...");
        progressDialog.show();

        String id = databaseReference.push().getKey();

        if (id == null) {
            progressDialog.dismiss();

            Toast.makeText(
                    this,
                    "Failed to generate category ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Category category = new Category();

        category.setId(id);
        category.setCategoryName(name);
        category.setDescription(description);
        category.setStatus(status);

        databaseReference.child(id)
                .setValue(category)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            AddCategoryActivity.this,
                            "Category Added Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AddCategoryActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                });

    }
}