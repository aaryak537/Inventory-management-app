package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryActionListener {

    RecyclerView recyclerCategory;
    EditText etSearchCategory;
    TextView tvTotalCategories;
    LinearLayout layoutEmpty;
    FloatingActionButton fabAddCategory;

    ArrayList<Category> categoryList;
    CategoryAdapter adapter;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        recyclerCategory = findViewById(R.id.recyclerCategory);
        etSearchCategory = findViewById(R.id.etSearchCategory);
        tvTotalCategories = findViewById(R.id.tvTotalCategories);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        recyclerCategory.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();

        adapter = new CategoryAdapter(this, categoryList, this);
        recyclerCategory.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Categories")
                .child(user.getUid());

        loadCategories();

        fabAddCategory.setOnClickListener(v -> {

            Intent intent = new Intent(CategoryActivity.this,
                    AddCategoryActivity.class);

            startActivity(intent);

        });

        etSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                adapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void loadCategories() {

        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                categoryList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Category category = ds.getValue(Category.class);

                    if (category != null) {

                        category.setId(ds.getKey());

                        categoryList.add(category);

                    }

                }

                adapter.notifyDataSetChanged();

                tvTotalCategories.setText(String.valueOf(categoryList.size()));

                if (categoryList.isEmpty()) {

                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerCategory.setVisibility(View.GONE);

                } else {

                    layoutEmpty.setVisibility(View.GONE);
                    recyclerCategory.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(CategoryActivity.this, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onEdit(Category category) {

        Toast.makeText(this,
                "Edit: " + category.getCategoryName(),
                Toast.LENGTH_SHORT).show();

        // TODO: Open EditCategoryActivity
    }

    @Override
    public void onDelete(Category category) {

        databaseReference.child(category.getId())
                .removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Category Deleted",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}