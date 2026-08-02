package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SupplierActivity extends AppCompatActivity {

    EditText searchSupplier;
    RecyclerView recyclerSupplier;
    FloatingActionButton fabAddSupplier;

    ArrayList<Supplier> supplierList;
    SupplierAdapter adapter;

    DatabaseReference supplierRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        searchSupplier = findViewById(R.id.searchSupplier);
        recyclerSupplier = findViewById(R.id.recyclerSupplier);
        fabAddSupplier = findViewById(R.id.fabAddSupplier);

        recyclerSupplier.setLayoutManager(new LinearLayoutManager(this));

        supplierList = new ArrayList<>();

        adapter = new SupplierAdapter(this, supplierList);

        recyclerSupplier.setAdapter(adapter);

        supplierRef = FirebaseDatabase.getInstance().getReference("Suppliers");

        loadSuppliers();

        fabAddSupplier.setOnClickListener(v -> {

            Intent intent = new Intent(SupplierActivity.this,
                    AddSupplierActivity.class
            );
            startActivity(intent);
        });

        searchSupplier.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSuppliers() {

        supplierRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        supplierList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Supplier supplier = ds.getValue(Supplier.class);

                            supplierList.add(supplier);
                        }
                        adapter.updateList(supplierList);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {}
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadSuppliers();
    }
}