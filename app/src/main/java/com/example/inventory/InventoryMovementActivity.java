package com.example.inventory;

import android.os.Bundle;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryMovementActivity extends AppCompatActivity {

    private RecyclerView recyclerMovements;
    private TextView tvEmpty;
    private TextView tvTotalMovements;

    private final List<InventoryMovement> movementList =
            new ArrayList<>();

    private InventoryMovementAdapter adapter;

    private DatabaseReference purchasesRef;
    private DatabaseReference salesRef;

    private int pendingLoads = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inventorymovement);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerMovements =
                findViewById(R.id.recyclerMovements);

        tvEmpty =
                findViewById(R.id.tvEmptyMovements);

        tvTotalMovements =
                findViewById(R.id.tvTotalMovements);

        recyclerMovements.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new InventoryMovementAdapter(movementList);

        recyclerMovements.setAdapter(adapter);

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

        String uid = user.getUid();

        purchasesRef =
                FirebaseDatabase.getInstance()
                        .getReference("Purchases")
                        .child(uid);

        salesRef =
                FirebaseDatabase.getInstance()
                        .getReference("Sales")
                        .child(uid);

        loadMovements();
    }

    private void loadMovements() {

        movementList.clear();
        pendingLoads = 2;

        loadPurchases();
        loadSales();
    }

    private void loadPurchases() {

        purchasesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        for (DataSnapshot data :
                                snapshot.getChildren()) {

                            Purchase purchase =
                                    data.getValue(Purchase.class);

                            if (purchase == null) {
                                continue;
                            }

                            String date =
                                    safe(purchase.getPurchaseDate());

                            movementList.add(
                                    new InventoryMovement(
                                            "Purchase",
                                            safe(purchase.getProductName()),
                                            date,
                                            Math.max(0, purchase.getQuantity()),
                                            purchase.getTotalAmount(),
                                            "Supplier: "
                                                    + safe(purchase.getSupplierName()),
                                            parsePurchaseDate(date)
                                    )
                            );
                        }

                        finishOneLoad();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {
                        Toast.makeText(
                                InventoryMovementActivity.this,
                                "Could not load purchases: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        finishOneLoad();
                    }
                }
        );
    }

    private void loadSales() {

        salesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        for (DataSnapshot data :
                                snapshot.getChildren()) {

                            Sale sale =
                                    data.getValue(Sale.class);

                            if (sale == null) {
                                continue;
                            }

                            String date =
                                    buildSaleDate(sale);

                            movementList.add(
                                    new InventoryMovement(
                                            "Sale",
                                            safe(sale.getProductName()),
                                            date,
                                            Math.max(0, sale.getQuantity()),
                                            sale.getTotalAmount(),
                                            "Customer: "
                                                    + safe(sale.getCustomerName()),
                                            sale.getTimestamp()
                                    )
                            );
                        }

                        finishOneLoad();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {
                        Toast.makeText(
                                InventoryMovementActivity.this,
                                "Could not load sales: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        finishOneLoad();
                    }
                }
        );
    }

    private void finishOneLoad() {

        pendingLoads--;

        if (pendingLoads > 0) {
            return;
        }

        Collections.sort(
                movementList,
                Comparator.comparingLong(
                        InventoryMovement::getSortTime
                ).reversed()
        );

        adapter.notifyDataSetChanged();

        tvTotalMovements.setText(
                movementList.size()
                        + " movement"
                        + (movementList.size() == 1 ? "" : "s")
        );

        tvEmpty.setVisibility(
                movementList.isEmpty()
                        ? TextView.VISIBLE
                        : TextView.GONE
        );
    }

    private String buildSaleDate(Sale sale) {

        if (sale.getSaleDate() != null &&
                !sale.getSaleDate().trim().isEmpty()) {

            if (sale.getSaleTime() != null &&
                    !sale.getSaleTime().trim().isEmpty()) {

                return sale.getSaleDate()
                        + " • "
                        + sale.getSaleTime();
            }

            return sale.getSaleDate();
        }

        if (sale.getTimestamp() > 0) {
            return new SimpleDateFormat(
                    "dd/MM/yyyy • hh:mm a",
                    Locale.getDefault()
            ).format(
                    new Date(sale.getTimestamp())
            );
        }

        return "-";
    }

    private long parsePurchaseDate(String date) {

        if (date == null || date.trim().isEmpty()) {
            return 0L;
        }

        try {
            Date parsed =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    ).parse(date.trim());

            return parsed == null ? 0L : parsed.getTime();

        } catch (ParseException ignored) {
            return 0L;
        }
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "-"
                : value.trim();
    }
}
