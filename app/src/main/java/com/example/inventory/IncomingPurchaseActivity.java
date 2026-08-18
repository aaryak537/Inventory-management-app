package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IncomingPurchaseActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private RecyclerView recyclerPurchases;

    private TextView tvEmptyPurchases;

    private ProgressBar progressBar;

    private ImageView btnBack;

    private FloatingActionButton fabAddPurchase;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;

    private DatabaseReference purchasesRef;


    // =========================================================
    // ADAPTER / LIST
    // =========================================================

    private PurchaseAdapter purchaseAdapter;

    private final ArrayList<Purchase> purchaseList =
            new ArrayList<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // IMPORTANT:
        // This must match activity_incoming_purchase.xml
        setContentView(
                R.layout.activity_incomingpurchase
        );


        // =====================================================
        // INITIALIZE FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        initializeViews();


        // =====================================================
        // ADD PURCHASE BUTTON
        // =====================================================

        fabAddPurchase.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    IncomingPurchaseActivity.this,
                                    AddPurchaseActivity.class
                            );

                    startActivity(intent);
                }
        );


        // =====================================================
        // FIREBASE PURCHASE REFERENCE
        // =====================================================

        String uid =
                auth.getCurrentUser().getUid();


        purchasesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Purchases")
                        .child(uid);


        // =====================================================
        // SETUP RECYCLER
        // =====================================================

        setupRecyclerView();


        // =====================================================
        // LOAD PURCHASES
        // =====================================================

        loadPurchases();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        fabAddPurchase =
                findViewById(
                        R.id.fabAddPurchase
                );


        recyclerPurchases =
                findViewById(
                        R.id.recyclerPurchases
                );


        tvEmptyPurchases =
                findViewById(
                        R.id.tvEmptyPurchases
                );


        progressBar =
                findViewById(
                        R.id.progressBar
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        recyclerPurchases.setLayoutManager(
                new LinearLayoutManager(this)
        );


        recyclerPurchases.setHasFixedSize(false);


        purchaseAdapter =
                new PurchaseAdapter(
                        this,
                        purchaseList
                );


        recyclerPurchases.setAdapter(
                purchaseAdapter
        );
    }


    // =========================================================
    // LOAD PURCHASES
    // =========================================================

    private void loadPurchases() {

        showLoading(true);


        purchasesRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        purchaseList.clear();


                        // =================================================
                        // READ PURCHASES
                        // =================================================

                        for (DataSnapshot purchaseSnapshot :
                                snapshot.getChildren()) {

                            Purchase purchase =
                                    purchaseSnapshot.getValue(
                                            Purchase.class
                                    );


                            if (purchase != null) {

                                // If purchaseId wasn't stored,
                                // use Firebase key.

                                if (purchase.getPurchaseId() == null
                                        || purchase.getPurchaseId()
                                        .isEmpty()) {

                                    purchase.setPurchaseId(
                                            purchaseSnapshot.getKey()
                                    );
                                }


                                purchaseList.add(
                                        purchase
                                );
                            }
                        }


                        // =================================================
                        // SORT — LATEST FIRST
                        // =================================================

                        Collections.sort(
                                purchaseList,
                                new Comparator<Purchase>() {

                                    @Override
                                    public int compare(
                                            Purchase p1,
                                            Purchase p2) {

                                        String date1 =
                                                p1.getPurchaseDate();

                                        String date2 =
                                                p2.getPurchaseDate();


                                        if (date1 == null) {
                                            date1 = "";
                                        }


                                        if (date2 == null) {
                                            date2 = "";
                                        }


                                        return date2.compareTo(
                                                date1
                                        );
                                    }
                                }
                        );


                        // =================================================
                        // UPDATE ADAPTER
                        // =================================================

                        purchaseAdapter.notifyDataSetChanged();


                        showLoading(false);


                        // =================================================
                        // EMPTY STATE
                        // =================================================

                        if (purchaseList.isEmpty()) {

                            tvEmptyPurchases.setVisibility(
                                    View.VISIBLE
                            );

                            recyclerPurchases.setVisibility(
                                    View.GONE
                            );

                        } else {

                            tvEmptyPurchases.setVisibility(
                                    View.GONE
                            );

                            recyclerPurchases.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showLoading(false);


                        Toast.makeText(
                                IncomingPurchaseActivity.this,
                                "Failed to load purchases: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();


                        tvEmptyPurchases.setVisibility(
                                View.VISIBLE
                        );


                        recyclerPurchases.setVisibility(
                                View.GONE
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOADING
    // =========================================================

    private void showLoading(boolean loading) {

        if (loading) {

            progressBar.setVisibility(
                    View.VISIBLE
            );

            recyclerPurchases.setVisibility(
                    View.GONE
            );

            tvEmptyPurchases.setVisibility(
                    View.GONE
            );

        } else {

            progressBar.setVisibility(
                    View.GONE
            );
        }
    }
}