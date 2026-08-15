package com.example.inventory;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditCategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private EditText etCategoryName;
    private EditText etCategoryDescription;

    private AutoCompleteTextView spStatus;

    private MaterialButton btnUpdateCategory;

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    private String categoryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editcategory);

        // =========================================
        // FIND VIEWS
        // =========================================

        btnBack = findViewById(R.id.btnBack);

        etCategoryName =
                findViewById(R.id.etCategoryName);

        etCategoryDescription =
                findViewById(R.id.etCategoryDescription);

        spStatus =
                findViewById(R.id.spStatus);

        btnUpdateCategory =
                findViewById(R.id.btnUpdateCategory);


        // =========================================
        // GET CURRENT USER
        // =========================================

        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }


        // =========================================
        // FIREBASE REFERENCE
        // =========================================

        databaseReference =
                FirebaseDatabase.getInstance()
                        .getReference("Categories")
                        .child(user.getUid());


        // =========================================
        // GET CATEGORY DATA
        // =========================================

        categoryId =
                getIntent().getStringExtra("categoryId");

        String categoryName =
                getIntent().getStringExtra("categoryName");

        String description =
                getIntent().getStringExtra("description");

        String status =
                getIntent().getStringExtra("status");


        // =========================================
        // CHECK CATEGORY ID
        // =========================================

        if (categoryId == null ||
                categoryId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Category ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }


        // =========================================
        // SET EXISTING VALUES
        // =========================================

        etCategoryName.setText(
                categoryName != null
                        ? categoryName
                        : ""
        );

        etCategoryDescription.setText(
                description != null
                        ? description
                        : ""
        );


        // =========================================
        // STATUS DROPDOWN
        // =========================================

        String[] statusList = {
                "Active",
                "Inactive"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statusList
                );

        spStatus.setAdapter(statusAdapter);

        if (status != null &&
                !status.isEmpty()) {

            spStatus.setText(
                    status,
                    false
            );
        } else {

            spStatus.setText(
                    "Active",
                    false
            );
        }


        // =========================================
        // BACK
        // =========================================

        btnBack.setOnClickListener(v ->
                finish()
        );


        // =========================================
        // UPDATE
        // =========================================

        btnUpdateCategory.setOnClickListener(v ->
                updateCategory()
        );


        // =========================================
        // PROGRESS DIALOG
        // =========================================

        progressDialog =
                new ProgressDialog(this);

        progressDialog.setCancelable(false);
    }


    // =========================================================
    // UPDATE CATEGORY
    // =========================================================

    private void updateCategory() {

        String name =
                etCategoryName
                        .getText()
                        .toString()
                        .trim();

        String description =
                etCategoryDescription
                        .getText()
                        .toString()
                        .trim();

        String status =
                spStatus
                        .getText()
                        .toString()
                        .trim();


        // =========================================
        // VALIDATION
        // =========================================

        if (TextUtils.isEmpty(name)) {

            etCategoryName.setError(
                    "Enter category name"
            );

            etCategoryName.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(status)) {

            Toast.makeText(
                    this,
                    "Select category status",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =========================================
        // SHOW PROGRESS
        // =========================================

        progressDialog.setMessage(
                "Updating Category..."
        );

        progressDialog.show();


        // =========================================
        // UPDATE ONLY REQUIRED FIELDS
        // =========================================

        databaseReference
                .child(categoryId)
                .child("categoryName")
                .setValue(name)
                .addOnSuccessListener(unused -> {

                    databaseReference
                            .child(categoryId)
                            .child("description")
                            .setValue(description)
                            .addOnSuccessListener(unused2 -> {

                                databaseReference
                                        .child(categoryId)
                                        .child("status")
                                        .setValue(status)
                                        .addOnSuccessListener(
                                                unused3 -> {

                                                    progressDialog
                                                            .dismiss();

                                                    Toast.makeText(
                                                            EditCategoryActivity.this,
                                                            "Category Updated Successfully",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    finish();
                                                }
                                        )
                                        .addOnFailureListener(e -> {

                                            progressDialog
                                                    .dismiss();

                                            Toast.makeText(
                                                    EditCategoryActivity.this,
                                                    e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        });
                            })
                            .addOnFailureListener(e -> {

                                progressDialog.dismiss();

                                Toast.makeText(
                                        EditCategoryActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    progressDialog.dismiss();

                    Toast.makeText(
                            EditCategoryActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}