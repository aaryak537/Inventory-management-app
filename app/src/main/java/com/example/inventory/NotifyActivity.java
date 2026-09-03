package com.example.inventory;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

public class NotifyActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotifyAdapter adapter;
    private ArrayList<NotifyModel> notifyList;
    private ImageButton btnBack, btnClear;
    private LinearLayout layoutEmpty;
    private DatabaseReference notificationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        setupRecyclerView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        notificationsRef = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(user.getUid());

        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifyList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotifyModel model = ds.getValue(NotifyModel.class);
                    if (model != null) notifyList.add(0, model);
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        NotifyActivity.this,
                        "Unable to load notifications: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            if (notificationsRef == null) return;

            notificationsRef.removeValue()
                    .addOnSuccessListener(unused -> {
                        notifyList.clear();
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(
                                this,
                                "Notifications cleared",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Unable to clear notifications: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        });
    }

    private void setupRecyclerView() {
        notifyList = new ArrayList<>();
        adapter = new NotifyAdapter(this, notifyList);
        recyclerNotifications.setLayoutManager(
                new LinearLayoutManager(this)
        );
        recyclerNotifications.setAdapter(adapter);
    }

    private void updateEmptyState() {
        if (notifyList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);
        }
    }
}
