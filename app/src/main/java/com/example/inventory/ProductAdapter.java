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

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ViewHolder>
        implements Filterable {

    // ============================================================
    // VARIABLES
    // ============================================================

    private final Context context;

    // Currently displayed list
    private final ArrayList<Product> list;

    // Complete unfiltered list
    private final ArrayList<Product> fullList;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public ProductAdapter(
            Context context,
            ArrayList<Product> list
    ) {

        this.context = context;

        this.list = new ArrayList<>();

        this.fullList = new ArrayList<>();

        if (list != null) {

            this.list.addAll(list);

            this.fullList.addAll(list);
        }
    }


    // ============================================================
    // UPDATE LIST
    //
    // IMPORTANT:
    // This method updates BOTH the displayed list and the
    // complete search list.
    // ============================================================

    public void updateList(
            ArrayList<Product> newList
    ) {

        fullList.clear();

        list.clear();

        if (newList != null) {

            fullList.addAll(newList);

            list.addAll(newList);
        }

        notifyDataSetChanged();
    }


    // ============================================================
    // ALTERNATIVE METHOD NAME
    //
    // Keeps compatibility if ProductActivity uses updateData().
    // ============================================================

    public void updateData(
            ArrayList<Product> newList
    ) {

        updateList(newList);
    }


    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_product,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }


    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        // --------------------------------------------------------
        // IMPORTANT
        //
        // Use a final reference so that this Product can safely
        // be used inside lambda expressions.
        // --------------------------------------------------------

        final Product product =
                list.get(position);


        // ========================================================
        // PRODUCT NAME
        // ========================================================

        String productName =
                product.getProductName();

        if (productName == null ||
                productName.trim().isEmpty()) {

            productName = "Unnamed Product";
        }

        holder.txtProductName.setText(
                productName
        );


        // ========================================================
        // CATEGORY
        // ========================================================

        String category =
                product.getCategory();

        if (category == null ||
                category.trim().isEmpty()) {

            category = "No Category";
        }

        holder.txtCategory.setText(
                category
        );


        // ========================================================
        // SELLING PRICE
        // ========================================================

        holder.txtPrice.setText(
                "₹" +
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                product.getSellingPrice()
                        )
        );


        // ========================================================
        // QUANTITY
        // ========================================================

        int quantity =
                product.getQuantity();

        holder.txtQuantity.setText(
                "Quantity : " + quantity
        );


        // ========================================================
        // STOCK STATUS
        //
        // Quantity is the source of truth.
        //
        // 0       = Out of Stock
        // 1 - 10  = Low Stock
        // 11+     = Available
        // ========================================================

        if (quantity <= 0) {

            holder.txtStatus.setText(
                    "Out of Stock"
            );

            holder.txtStatus.setTextColor(
                    Color.WHITE
            );

            holder.txtStatus.setBackgroundColor(
                    Color.RED
            );

        } else if (quantity <= 10) {

            holder.txtStatus.setText(
                    "Low Stock"
            );

            holder.txtStatus.setTextColor(
                    Color.WHITE
            );

            holder.txtStatus.setBackgroundColor(
                    Color.RED
            );

        } else {

            holder.txtStatus.setText(
                    "Available"
            );

            holder.txtStatus.setTextColor(
                    Color.WHITE
            );

            holder.txtStatus.setBackgroundResource(
                    R.drawable.circle_green_bg
            );
        }


        // ========================================================
        // EDIT PRODUCT
        // ========================================================

        holder.btnEdit.setOnClickListener(
                v -> {

                    // Product is final, therefore Java allows
                    // it inside this lambda.

                    Intent intent =
                            new Intent(
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
                }
        );


        // ========================================================
        // DELETE PRODUCT
        // ========================================================

        holder.btnDelete.setOnClickListener(
                v -> {

                    new AlertDialog.Builder(context)

                            .setTitle(
                                    "Delete Product"
                            )

                            .setMessage(
                                    "Are you sure you want to delete this product?"
                            )

                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        deleteProduct(
                                                product
                                        );
                                    }
                            )

                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )

                            .show();
                }
        );
    }


    // ============================================================
    // DELETE PRODUCT
    // ============================================================

    private void deleteProduct(
            final Product product
    ) {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        // ========================================================
        // USER CHECK
        // ========================================================

        if (user == null) {

            Toast.makeText(
                    context,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ========================================================
        // PRODUCT ID
        // ========================================================

        final String productId =
                product.getProductId();


        if (productId == null ||
                productId.trim().isEmpty()) {

            Toast.makeText(
                    context,
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ========================================================
        // FIREBASE DELETE
        // ========================================================

        FirebaseDatabase
                .getInstance()
                .getReference("Products")
                .child(user.getUid())
                .child(productId)
                .removeValue()

                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    context,
                                    "Product Deleted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Remove from both lists locally.
                            removeProductLocally(
                                    product
                            );
                        }
                )

                .addOnFailureListener(
                        e -> {

                            String errorMessage =
                                    e.getMessage();

                            if (errorMessage == null ||
                                    errorMessage.trim().isEmpty()) {

                                errorMessage =
                                        "Unknown error";
                            }

                            Toast.makeText(
                                    context,
                                    "Delete failed: "
                                            + errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // ============================================================
    // REMOVE PRODUCT LOCALLY
    // ============================================================

    private void removeProductLocally(
            Product product
    ) {

        // Remove using product ID rather than object reference.
        // This is safer if Firebase recreated the object.

        String productId =
                product.getProductId();


        // --------------------------------------------------------
        // Remove from complete list
        // --------------------------------------------------------

        for (int i = fullList.size() - 1; i >= 0; i--) {

            Product item =
                    fullList.get(i);

            if (item == null) {
                continue;
            }

            String id =
                    item.getProductId();

            if (productId != null &&
                    productId.equals(id)) {

                fullList.remove(i);
            }
        }


        // --------------------------------------------------------
        // Remove from displayed list
        // --------------------------------------------------------

        for (int i = list.size() - 1; i >= 0; i--) {

            Product item =
                    list.get(i);

            if (item == null) {
                continue;
            }

            String id =
                    item.getProductId();

            if (productId != null &&
                    productId.equals(id)) {

                list.remove(i);
            }
        }


        notifyDataSetChanged();
    }


    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return list.size();
    }


    // ============================================================
    // VIEW HOLDER
    // ============================================================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtProductName;
        TextView txtCategory;
        TextView txtPrice;
        TextView txtQuantity;
        TextView txtStatus;

        ImageView btnEdit;
        ImageView btnDelete;


        ViewHolder(
                @NonNull View itemView
        ) {

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


    // ============================================================
    // SEARCH
    // ============================================================

    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(
                    CharSequence constraint
            ) {

                ArrayList<Product> filteredList =
                        new ArrayList<>();


                // =================================================
                // EMPTY SEARCH
                //
                // Return the COMPLETE original list.
                // =================================================

                if (constraint == null ||
                        constraint.toString().trim().isEmpty()) {

                    filteredList.addAll(
                            fullList
                    );

                } else {

                    String searchText =
                            constraint
                                    .toString()
                                    .trim()
                                    .toLowerCase(
                                            Locale.getDefault()
                                    );


                    // =============================================
                    // SEARCH COMPLETE LIST
                    // =============================================

                    for (Product product :
                            fullList) {

                        if (product == null) {
                            continue;
                        }


                        // -----------------------------------------
                        // PRODUCT NAME
                        // -----------------------------------------

                        String name =
                                product.getProductName();

                        if (name == null) {
                            name = "";
                        }


                        name =
                                name.toLowerCase(
                                        Locale.getDefault()
                                );


                        // -----------------------------------------
                        // CATEGORY
                        // -----------------------------------------

                        String category =
                                product.getCategory();

                        if (category == null) {
                            category = "";
                        }


                        category =
                                category.toLowerCase(
                                        Locale.getDefault()
                                );


                        // -----------------------------------------
                        // BRAND
                        // -----------------------------------------

                        String brand =
                                product.getBrandName();

                        if (brand == null) {
                            brand = "";
                        }


                        brand =
                                brand.toLowerCase(
                                        Locale.getDefault()
                                );


                        // -----------------------------------------
                        // PRODUCT ID
                        // -----------------------------------------

                        String productId =
                                product.getProductId();

                        if (productId == null) {
                            productId = "";
                        }


                        productId =
                                productId.toLowerCase(
                                        Locale.getDefault()
                                );


                        // -----------------------------------------
                        // MATCH
                        // -----------------------------------------

                        if (name.contains(searchText)
                                || category.contains(searchText)
                                || brand.contains(searchText)
                                || productId.contains(searchText)) {

                            filteredList.add(
                                    product
                            );
                        }
                    }
                }


                // =================================================
                // FILTER RESULTS
                // =================================================

                FilterResults results =
                        new FilterResults();

                results.values =
                        filteredList;

                results.count =
                        filteredList.size();

                return results;
            }


            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(
                    CharSequence constraint,
                    FilterResults results
            ) {

                list.clear();


                if (results != null &&
                        results.values != null) {

                    ArrayList<Product> filteredList =
                            (ArrayList<Product>)
                                    results.values;

                    list.addAll(
                            filteredList
                    );
                }


                notifyDataSetChanged();
            }
        };
    }
}