package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SupplierActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private EditText searchSupplier;

    private RecyclerView recyclerSupplier;

    private FloatingActionButton fabAddSupplier;

    // Statistics
    private TextView tvTotalSuppliers;
    private TextView tvSupplierCompanies;
    private TextView tvSuppliersWithEmail;


    // =====================================================
    // DATA
    // =====================================================

    private ArrayList<Supplier> supplierList;

    private SupplierAdapter adapter;

    private DatabaseReference supplierRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_supplier);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        searchSupplier =
                findViewById(R.id.searchSupplier);

        recyclerSupplier =
                findViewById(R.id.recyclerSupplier);

        fabAddSupplier =
                findViewById(R.id.fabAddSupplier);


        // Statistics

        tvTotalSuppliers =
                findViewById(R.id.tvTotalSuppliers);

        tvSupplierCompanies =
                findViewById(R.id.tvSupplierCompanies);

        tvSuppliersWithEmail =
                findViewById(R.id.tvSuppliersWithEmail);


        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerSupplier.setLayoutManager(
                new LinearLayoutManager(this)
        );


        // =====================================================
        // SUPPLIER LIST
        // =====================================================

        supplierList =
                new ArrayList<>();


        adapter =
                new SupplierAdapter(
                        this,
                        supplierList
                );


        recyclerSupplier.setAdapter(adapter);


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
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


        // =====================================================
        // FIREBASE REFERENCE
        // =====================================================

        /*
         *
         * Firebase structure:
         *
         * Suppliers
         *      |
         *      └── USER_UID
         *             |
         *             ├── SUPPLIER_ID
         *             ├── SUPPLIER_ID
         *             └── SUPPLIER_ID
         *
         */

        supplierRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Suppliers")
                        .child(user.getUid());


        // =====================================================
        // LOAD SUPPLIERS
        // =====================================================

        loadSuppliers();


        // =====================================================
        // ADD SUPPLIER BUTTON
        // =====================================================

        fabAddSupplier.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SupplierActivity.this,
                            AddSupplierActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // SEARCH SUPPLIER
        // =====================================================

        searchSupplier.addTextChangedListener(
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

                        adapter
                                .getFilter()
                                .filter(s);
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }


    // =========================================================
    // LOAD SUPPLIERS FROM FIREBASE
    // =========================================================

    private void loadSuppliers() {

        supplierRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        // Clear old data
                        supplierList.clear();


                        // =================================================
                        // STATISTICS
                        // =================================================

                        int totalSuppliers = 0;

                        int suppliersWithEmail = 0;


                        /*
                         * HashSet prevents duplicate company
                         * names from being counted twice.
                         */

                        Set<String> companies =
                                new HashSet<>();


                        // =================================================
                        // READ SUPPLIERS
                        // =================================================

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {


                            Supplier supplier =
                                    ds.getValue(
                                            Supplier.class
                                    );


                            if (supplier == null) {
                                continue;
                            }


                            // =============================================
                            // SUPPLIER ID
                            // =============================================

                            /*
                             * If ID doesn't exist inside
                             * Firebase, use the child key.
                             */

                            if (supplier.getId() == null
                                    ||
                                    supplier.getId()
                                            .trim()
                                            .isEmpty()) {

                                supplier.setId(
                                        ds.getKey()
                                );
                            }


                            // =============================================
                            // ADD TO LIST
                            // =============================================

                            supplierList.add(
                                    supplier
                            );


                            // =============================================
                            // TOTAL SUPPLIERS
                            // =============================================

                            totalSuppliers++;


                            // =============================================
                            // COMPANY COUNT
                            // =============================================

                            String company =
                                    supplier.getCompany();


                            if (company != null
                                    &&
                                    !company
                                            .trim()
                                            .isEmpty()) {

                                companies.add(
                                        company
                                                .trim()
                                                .toLowerCase()
                                );
                            }


                            // =============================================
                            // EMAIL COUNT
                            // =============================================

                            String email =
                                    supplier.getEmail();


                            if (email != null
                                    &&
                                    !email
                                            .trim()
                                            .isEmpty()) {

                                suppliersWithEmail++;
                            }
                        }


                        // =================================================
                        // UPDATE RECYCLER
                        // =================================================

                        adapter.updateList(
                                supplierList
                        );


                        // =================================================
                        // UPDATE TOTAL
                        // =================================================

                        tvTotalSuppliers.setText(
                                String.valueOf(
                                        totalSuppliers
                                )
                        );


                        // =================================================
                        // UPDATE COMPANIES
                        // =================================================

                        tvSupplierCompanies.setText(
                                String.valueOf(
                                        companies.size()
                                )
                        );


                        // =================================================
                        // UPDATE EMAIL COUNT
                        // =================================================

                        tvSuppliersWithEmail.setText(
                                String.valueOf(
                                        suppliersWithEmail
                                )
                        );
                    }


                    // =====================================================
                    // FIREBASE ERROR
                    // =====================================================

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                SupplierActivity.this,
                                "Failed to load suppliers: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}