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

    public void updateList(ArrayList<Product> newList) {

        list.clear();
        list.addAll(newList);

        fullList.clear();
        fullList.addAll(newList);

        notifyDataSetChanged();
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
       // holder.tvProductNo.setText(product.getProID());
        holder.txtPrice.setText("₹" + String.format("%.2f", product.getSellingPrice()));
        holder.txtQuantity.setText("Quantity : " + product.getQuantity());

        if (product.isInStock()) {

            holder.txtStatus.setText("Available");
            holder.txtStatus.setBackgroundResource(R.drawable.circle_green_bg);
        } else {
            holder.txtStatus.setText("Low Stock");
            holder.txtStatus.setBackgroundColor(Color.RED);
        }

        holder.btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(context, EditProActivity.class);

            intent.putExtra("productId", product.getProductId());

            intent.putExtra("name", product.getProductName());

            intent.putExtra("category", product.getCategory());

            intent.putExtra("price", product.getSellingPrice());

            intent.putExtra("quantity", product.getQuantity());

            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Delete this product?")
                    .setPositiveButton("Delete",
                            (dialog, which) -> {

                                FirebaseUser user = FirebaseAuth.getInstance()
                                                .getCurrentUser();

                                if (user == null)
                                    return;

                                FirebaseDatabase.getInstance()
                                        .getReference("Products")
                                        .child(user.getUid())
                                        .child(product.getProductId())
                                        .removeValue()
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(context, "Product Deleted",
                                                                Toast.LENGTH_SHORT)
                                                        .show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(context, e.getMessage(),
                                                                Toast.LENGTH_SHORT)
                                                        .show());
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
        TextView txtProductName, txtCategory, txtPrice, txtQuantity, txtStatus;
        ImageView btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
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

                ArrayList<Product> filtered = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {

                    filtered.addAll(fullList);

                } else {
                    String text = constraint.toString()
                            .toLowerCase().trim();

                    for (Product product : fullList) {

                        if (product.getProductName().toLowerCase()
                                .contains(text)
                                ||
                                product.getCategory().toLowerCase()
                                        .contains(text)) {

                            filtered.add(product);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                list.clear();
                list.addAll((ArrayList<Product>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}