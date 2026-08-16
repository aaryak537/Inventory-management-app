package com.example.inventory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>
        implements Filterable {

    private final Context context;

    // Currently displayed products
    private final ArrayList<Product> list;

    // Complete product list used for searching
    private final ArrayList<Product> fullList;

    public ProductAdapter(Context context, ArrayList<Product> list) {

        this.context = context;
        this.list = list;

        // Keep a separate copy for search
        this.fullList = new ArrayList<>(list);
    }

    // =========================================================
    // UPDATE PRODUCT LIST
    // =========================================================

    public void updateList(ArrayList<Product> newList) {

        // Update displayed list
        list.clear();
        list.addAll(newList);

        // IMPORTANT:
        // Update fullList too so search works correctly
        fullList.clear();
        fullList.addAll(newList);

        notifyDataSetChanged();
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);

        return new ViewHolder(view);
    }

    // =========================================================
    // BIND PRODUCT DATA
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Product product = list.get(position);

        // Product name
        String productName = product.getProductName();

        if (productName == null || productName.trim().isEmpty()) {
            productName = "Unnamed Product";
        }

        holder.txtProductName.setText(productName);

        // Category
        String category = product.getCategory();

        if (category == null || category.trim().isEmpty()) {
            category = "No Category";
        }

        holder.txtCategory.setText(category);

        // Selling price
        holder.txtPrice.setText(
                "₹" + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        product.getSellingPrice()
                )
        );

        // Quantity
        holder.txtQuantity.setText(
                "Quantity : " + product.getQuantity()
        );

        // =====================================================
        // STOCK STATUS
        // =====================================================

        if (product.isInStock()) {

            holder.txtStatus.setText("Available");

            holder.txtStatus.setBackgroundResource(
                    R.drawable.circle_green_bg
            );

        } else {

            holder.txtStatus.setText("Low Stock");

            holder.txtStatus.setBackgroundColor(Color.RED);
        }

        // =====================================================
        // EDIT PRODUCT
        // =====================================================

        holder.btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    EditProActivity.class
            );

            intent.putExtra(
                    "productId",
                    product.getProductId()
            );

            intent.putExtra(
                    "name",
                    product.getProductName()
            );

            intent.putExtra(
                    "category",
                    product.getCategory()
            );

            intent.putExtra(
                    "price",
                    product.getSellingPrice()
            );

            intent.putExtra(
                    "quantity",
                    product.getQuantity()
            );

            context.startActivity(intent);
        });

        // =====================================================
        // DELETE PRODUCT
        // =====================================================

        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Delete this product?")
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                FirebaseUser user =
                                        FirebaseAuth
                                                .getInstance()
                                                .getCurrentUser();

                                if (user == null) {

                                    Toast.makeText(
                                            context,
                                            "User not logged in",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                String productId =
                                        product.getProductId();

                                if (productId == null ||
                                        productId.isEmpty()) {

                                    Toast.makeText(
                                            context,
                                            "Product ID not found",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("Products")
                                        .child(user.getUid())
                                        .child(productId)
                                        .removeValue()

                                        .addOnSuccessListener(unused -> {

                                            Toast.makeText(
                                                    context,
                                                    "Product Deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                        })

                                        .addOnFailureListener(e -> {

                                            Toast.makeText(
                                                    context,
                                                    "Delete failed: "
                                                            + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        });
                            }
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .show();
        });
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return list.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtProductName;
        TextView txtCategory;
        TextView txtPrice;
        TextView txtQuantity;
        TextView txtStatus;

        ImageView btnEdit;
        ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {

            super(itemView);

            txtProductName =
                    itemView.findViewById(
                            R.id.txtProductName
                    );

            txtCategory =
                    itemView.findViewById(
                            R.id.txtCategory
                    );

            txtPrice =
                    itemView.findViewById(
                            R.id.txtPrice
                    );

            txtQuantity =
                    itemView.findViewById(
                            R.id.txtQuantity
                    );

            txtStatus =
                    itemView.findViewById(
                            R.id.txtStatus
                    );

            btnEdit =
                    itemView.findViewById(
                            R.id.btnEdit
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDelete
                    );
        }
    }

    // =========================================================
    // SEARCH FILTER
    // =========================================================

    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(
                    CharSequence constraint) {

                ArrayList<Product> filtered =
                        new ArrayList<>();

                // No search text
                if (constraint == null ||
                        constraint.length() == 0) {

                    filtered.addAll(fullList);

                } else {

                    String text =
                            constraint.toString()
                                    .toLowerCase(Locale.getDefault())
                                    .trim();

                    // Search through complete list
                    for (Product product : fullList) {

                        String name =
                                product.getProductName() == null
                                        ? ""
                                        : product.getProductName()
                                          .toLowerCase(
                                                  Locale.getDefault()
                                          );

                        String category =
                                product.getCategory() == null
                                        ? ""
                                        : product.getCategory()
                                          .toLowerCase(
                                                  Locale.getDefault()
                                          );

                        if (name.contains(text) ||
                                category.contains(text)) {

                            filtered.add(product);
                        }
                    }
                }

                FilterResults results =
                        new FilterResults();

                results.values = filtered;
                results.count = filtered.size();

                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(
                    CharSequence constraint,
                    FilterResults results) {

                list.clear();

                if (results.values != null) {

                    list.addAll(
                            (ArrayList<Product>)
                                    results.values
                    );
                }

                notifyDataSetChanged();
            }
        };
    }
}