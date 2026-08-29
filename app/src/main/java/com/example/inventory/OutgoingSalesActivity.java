package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OutgoingSalesActivity extends AppCompatActivity {





    private ImageView btnBack;
    private ImageView btnAddSale;

    private EditText etSearchSales;

    private TextView tvTodaySales;
    private TextView tvProductsSold;
    private TextView tvTransactions;

    private RecyclerView recyclerSales;
    private LinearLayout emptyState;






    private FirebaseAuth firebaseAuth;
    private DatabaseReference salesRef;






    private SaleAdapter saleAdapter;

    private final List<Sale> saleList =
            new ArrayList<>();

    private final List<Sale> filteredSaleList =
            new ArrayList<>();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_outgoingsales
        );

        initializeViews();

        setupFirebase();

        setupRecyclerView();

        setupListeners();

        loadSales();
    }






    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnAddSale =
                findViewById(R.id.btnAddSale);

        etSearchSales =
                findViewById(R.id.etSearchSales);

        tvTodaySales =
                findViewById(R.id.tvTodaySales);

        tvProductsSold =
                findViewById(R.id.tvProductsSold);

        tvTransactions =
                findViewById(R.id.tvTransactions);

        recyclerSales =
                findViewById(R.id.recyclerSales);

        emptyState =
                findViewById(R.id.emptyState);
    }






    private void setupFirebase() {

        firebaseAuth =
                FirebaseAuth.getInstance();

        FirebaseUser user =
                firebaseAuth.getCurrentUser();


        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        String userId =
                user.getUid();










        salesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Sales")
                        .child(userId);
    }






    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        recyclerSales.setLayoutManager(
                layoutManager
        );


        saleAdapter =
                new SaleAdapter(
                        this,
                        filteredSaleList
                );

        recyclerSales.setAdapter(
                saleAdapter
        );
    }






    private void setupListeners() {





        btnBack.setOnClickListener(v ->
                finish()
        );






        btnAddSale.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            OutgoingSalesActivity.this,
                            AddSaleActivity.class
                    );

            startActivity(intent);
        });






        etSearchSales.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterSales(
                                s.toString()
                        );
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }






    private void loadSales() {

        if (salesRef == null) {
            return;
        }


        salesRef
                .orderByChild("timestamp")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                saleList.clear();


                                for (
                                        DataSnapshot dataSnapshot :
                                        snapshot.getChildren()
                                ) {

                                    Sale sale =
                                            dataSnapshot
                                                    .getValue(
                                                            Sale.class
                                                    );


                                    if (sale != null) {


                                        sale.setSaleId(
                                                dataSnapshot.getKey()
                                        );

                                        saleList.add(
                                                sale
                                        );
                                    }
                                }



                                saleList.sort(
                                        (sale1, sale2) ->
                                                Long.compare(
                                                        sale2.getTimestamp(),
                                                        sale1.getTimestamp()
                                                )
                                );


                                updateSalesData();
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                Toast.makeText(
                                        OutgoingSalesActivity.this,
                                        "Failed to load sales: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }






    private void updateSalesData() {

        updateSummary();


        filteredSaleList.clear();

        filteredSaleList.addAll(
                saleList
        );


        saleAdapter.notifyDataSetChanged();

        updateEmptyState();
    }






    private void updateSummary() {

        double todaySales = 0;

        int productsSold = 0;

        int transactions =
                saleList.size();


        String today =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        for (Sale sale : saleList) {

            if (sale == null) {
                continue;
            }



            productsSold +=
                    sale.getQuantity();



            if (
                    sale.getSaleDate() != null
                            &&
                            sale.getSaleDate()
                                    .equals(today)
            ) {

                todaySales +=
                        sale.getTotalAmount();
            }
        }


        tvTodaySales.setText(
                "₹" +
                        formatAmount(
                                todaySales
                        )
        );


        tvProductsSold.setText(
                String.valueOf(
                        productsSold
                )
        );


        tvTransactions.setText(
                String.valueOf(
                        transactions
                )
        );
    }






    private void filterSales(
            String query
    ) {

        String search =
                query
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );


        filteredSaleList.clear();


        if (search.isEmpty()) {

            filteredSaleList.addAll(
                    saleList
            );

        } else {

            for (Sale sale :
                    saleList) {

                if (sale == null) {
                    continue;
                }


                String productName =
                        sale.getProductName() != null
                                ?
                                sale.getProductName()
                                .toLowerCase(
                                        Locale.getDefault()
                                )
                                :
                                "";


                String customerName =
                        sale.getCustomerName() != null
                                ?
                                sale.getCustomerName()
                                .toLowerCase(
                                        Locale.getDefault()
                                )
                                :
                                "";


                String paymentMethod =
                        sale.getPaymentMethod() != null
                                ?
                                sale.getPaymentMethod()
                                .toLowerCase(
                                        Locale.getDefault()
                                )
                                :
                                "";


                if (
                        productName.contains(search)
                                ||
                                customerName.contains(search)
                                ||
                                paymentMethod.contains(search)
                ) {

                    filteredSaleList.add(
                            sale
                    );
                }
            }
        }


        saleAdapter.notifyDataSetChanged();

        updateEmptyState();
    }






    private void updateEmptyState() {

        if (filteredSaleList.isEmpty()) {

            recyclerSales.setVisibility(
                    View.GONE
            );

            emptyState.setVisibility(
                    View.VISIBLE
            );

        } else {

            recyclerSales.setVisibility(
                    View.VISIBLE
            );

            emptyState.setVisibility(
                    View.GONE
            );
        }
    }






    private String formatAmount(
            double amount
    ) {

        if (
                amount ==
                        (long) amount
        ) {

            return String.format(
                    Locale.getDefault(),
                    "%d",
                    (long) amount
            );

        } else {

            return String.format(
                    Locale.getDefault(),
                    "%.2f",
                    amount
            );
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();


    }
}