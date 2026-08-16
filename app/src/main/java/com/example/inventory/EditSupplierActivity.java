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

import java.util.HashMap;

public class EditSupplierActivity extends AppCompatActivity {

    private EditText etSupplierName;
    private EditText etCompany;
    private EditText etPhone;
    private EditText etEmail;

    private Button btnUpdateSupplier;

    private DatabaseReference supplierRef;

    private String supplierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_editsupplier
        );

        etSupplierName =
                findViewById(
                        R.id.etSupplierName
                );

        etCompany =
                findViewById(
                        R.id.etCompany
                );

        etPhone =
                findViewById(
                        R.id.etPhone
                );

        etEmail =
                findViewById(
                        R.id.etEmail
                );

        btnUpdateSupplier =
                findViewById(
                        R.id.btnUpdateSupplier
                );

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

        // Receive supplier information
        if (getIntent() != null) {

            supplierId =
                    getIntent()
                            .getStringExtra("id");

            String name =
                    getIntent()
                            .getStringExtra("name");

            String company =
                    getIntent()
                            .getStringExtra("company");

            String phone =
                    getIntent()
                            .getStringExtra("phone");

            String email =
                    getIntent()
                            .getStringExtra("email");

            if (supplierId == null
                    ||
                    supplierId.trim().isEmpty()) {

                Toast.makeText(
                        this,
                        "Supplier ID not found",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
                return;
            }

            etSupplierName.setText(
                    name == null ? "" : name
            );

            etCompany.setText(
                    company == null ? "" : company
            );

            etPhone.setText(
                    phone == null ? "" : phone
            );

            etEmail.setText(
                    email == null ? "" : email
            );
        }

        btnUpdateSupplier.setOnClickListener(
                v -> updateSupplier()
        );
    }

    private void updateSupplier() {

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
                    "Enter a valid phone number"
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
                    "Enter a valid email address"
            );

            etEmail.requestFocus();
            return;
        }

        if (supplierId == null
                ||
                supplierId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid supplier ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnUpdateSupplier.setEnabled(false);

        HashMap<String, Object> map =
                new HashMap<>();

        map.put("id", supplierId);
        map.put("name", name);
        map.put("company", company);
        map.put("phone", phone);
        map.put("email", email);

        supplierRef
                .child(supplierId)
                .updateChildren(map)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    EditSupplierActivity.this,
                                    "Supplier Updated Successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnUpdateSupplier
                                    .setEnabled(true);

                            Toast.makeText(
                                    EditSupplierActivity.this,
                                    "Update failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
}