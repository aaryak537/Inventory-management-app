package com.example.inventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddSupplierActivity extends AppCompatActivity {

    private EditText etSupplierName;
    private EditText etCompany;
    private EditText etPhone;
    private EditText etEmail;

    private Button btnSaveSupplier;

    private DatabaseReference supplierRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addsupplier);

        etSupplierName = findViewById(R.id.etSupplierName);
        etCompany = findViewById(R.id.etCompany);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);

        btnSaveSupplier = findViewById(R.id.btnSaveSupplier);

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



        supplierRef =
                FirebaseDatabase.getInstance()
                        .getReference("Suppliers")
                        .child(user.getUid());

        btnSaveSupplier.setOnClickListener(
                v -> saveSupplier()
        );
    }

    private void saveSupplier() {

        String name =
                etSupplierName.getText()
                        .toString()
                        .trim();

        String company =
                etCompany.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();


        if (TextUtils.isEmpty(name)) {

            etSupplierName.setError(
                    "Enter supplier name"
            );

            etSupplierName.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(company)) {

            etCompany.setError(
                    "Enter company name"
            );

            etCompany.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(phone)) {

            etPhone.setError(
                    "Enter phone number"
            );

            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {

            etPhone.setError(
                    "Enter valid phone number"
            );

            etPhone.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Enter email address"
            );

            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Enter valid email address"
            );

            etEmail.requestFocus();
            return;
        }

        btnSaveSupplier.setEnabled(false);


        String supplierId =
                supplierRef.push().getKey();

        if (supplierId == null) {

            btnSaveSupplier.setEnabled(true);

            Toast.makeText(
                    this,
                    "Unable to generate supplier ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Supplier supplier =
                new Supplier(
                        supplierId,
                        name,
                        company,
                        phone,
                        email
                );


        supplierRef
                .child(supplierId)
                .setValue(supplier)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            AddSupplierActivity.this,
                            "Supplier Added Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnSaveSupplier.setEnabled(true);

                    Toast.makeText(
                            AddSupplierActivity.this,
                            "Failed to add supplier: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}