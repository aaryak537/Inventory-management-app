package com.example.inventory;

import androidx.appcompat.app.AlertDialog;
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

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>
        implements Filterable {

    private final Context context;
    private final ArrayList<Product> list;
    private final ArrayList<Product> fullList;

    public ProductAdapter(Context context, ArrayList<Product> list) {
        this.context = context;
        this.list = list;
        this.fullList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = list.get(position);

        holder.txtProductName.setText(product.getProductName());
        holder.txtCategory.setText(product.getCategory());

        holder.txtPrice.setText("₹" +
                String.format("%.2f", product.getSellingPrice()));

        holder.txtQuantity.setText("Quantity : " +
                product.getQuantity());

        if (product.isInStock()) {

            holder.txtStatus.setText("Available");
            holder.txtStatus.setBackgroundResource(R.drawable.circle_green_bg);

        } else {

            holder.txtStatus.setText("Out of Stock");
            holder.txtStatus.setBackgroundColor(Color.RED);
        }

        holder.btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(context, EditProActivity.class);

            intent.putExtra("name", product.getProductName());
            intent.putExtra("price", product.getSellingPrice());
            intent.putExtra("quantity", product.getQuantity());
            intent.putExtra("category", product.getCategory());

            context.startActivity(intent);

        });

        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("Products")
                               // .child(product.getId())   // Product ID stored in Firebase
                                .removeValue()
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(context,
                                            "Product deleted successfully",
                                            Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(context,
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtProductName;
        TextView txtCategory;
        TextView txtPrice;
        TextView txtQuantity;
        TextView txtStatus;

        ImageView btnEdit;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtStatus = itemView.findViewById(R.id.txtStatus);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                ArrayList<Product> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {

                    filteredList.addAll(fullList);

                } else {

                    String search =
                            constraint.toString().toLowerCase().trim();

                    for (Product product : fullList) {

                        if (product.getProductName().toLowerCase().contains(search)
                                || product.getCategory().toLowerCase().contains(search)) {

                            filteredList.add(product);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {

                list.clear();
                list.addAll((ArrayList<Product>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}