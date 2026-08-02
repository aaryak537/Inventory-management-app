package com.example.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {

            // TODO: Open Edit Product Activity

        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {

            // TODO: Delete Product

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