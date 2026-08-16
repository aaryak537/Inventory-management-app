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


    private EditText searchSupplier;

    private RecyclerView recyclerSupplier;

    private FloatingActionButton fabAddSupplier;


    private TextView tvTotalSuppliers;
    private TextView tvSupplierCompanies;
    private TextView tvSuppliersWithEmail;




    private ArrayList<Supplier> supplierList;

    private SupplierAdapter adapter;

    private DatabaseReference supplierRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_supplier);




        searchSupplier =
                findViewById(R.id.searchSupplier);

        recyclerSupplier =
                findViewById(R.id.recyclerSupplier);

        fabAddSupplier =
                findViewById(R.id.fabAddSupplier);




        tvTotalSuppliers =
                findViewById(R.id.tvTotalSuppliers);

        tvSupplierCompanies =
                findViewById(R.id.tvSupplierCompanies);

        tvSuppliersWithEmail =
                findViewById(R.id.tvSuppliersWithEmail);




        recyclerSupplier.setLayoutManager(
                new LinearLayoutManager(this)
        );




        supplierList =
                new ArrayList<>();


        adapter =
                new SupplierAdapter(
                        this,
                        supplierList
                );


        recyclerSupplier.setAdapter(adapter);



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




        supplierRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Suppliers")
                        .child(user.getUid());



        loadSuppliers();



        fabAddSupplier.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SupplierActivity.this,
                            AddSupplierActivity.class
                    );

            startActivity(intent);
        });



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



    private void loadSuppliers() {

        supplierRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {


                        supplierList.clear();




                        int totalSuppliers = 0;

                        int suppliersWithEmail = 0;




                        Set<String> companies =
                                new HashSet<>();



                        for (DataSnapshot ds :
                                snapshot.getChildren()) {


                            Supplier supplier =
                                    ds.getValue(
                                            Supplier.class
                                    );


                            if (supplier == null) {
                                continue;
                            }




                            if (supplier.getId() == null
                                    ||
                                    supplier.getId()
                                            .trim()
                                            .isEmpty()) {

                                supplier.setId(
                                        ds.getKey()
                                );
                            }


                            supplierList.add(
                                    supplier
                            );



                            totalSuppliers++;


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



                        adapter.updateList(
                                supplierList
                        );



                        tvTotalSuppliers.setText(
                                String.valueOf(
                                        totalSuppliers
                                )
                        );



                        tvSupplierCompanies.setText(
                                String.valueOf(
                                        companies.size()
                                )
                        );



                        tvSuppliersWithEmail.setText(
                                String.valueOf(
                                        suppliersWithEmail
                                )
                        );
                    }



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