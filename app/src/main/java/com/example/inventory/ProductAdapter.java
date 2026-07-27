package com.example.inventory;

import android.content.Context;
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

    Context context;

    ArrayList<Product> list;
    ArrayList<Product> fullList;

    public ProductAdapter(Context context, ArrayList<Product> list) {

        this.context = context;
        this.list = list;
        fullList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = list.get(position);

      //  holder.txtProductId.setText(product.getProId());
        holder.txtProductName.setText(product.getProductName());
        holder.txtCategory.setText(product.getCategory());
        holder.txtPrice.setText("₹" + product.getSellingPrice());
        holder.txtStock.setText("Stock : " + product.getStock());

        if(product.isInStock()){
            holder.txtStatus.setText("In Stock");
        }else{
            holder.txtStatus.setText("Out of Stock");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtProductId,txtProductName,txtCategory,txtPrice,txtStock,txtStatus;
        ImageView btnEdit,btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductId=itemView.findViewById(R.id.txtProductId);
            txtProductName=itemView.findViewById(R.id.txtProductName);
            txtCategory=itemView.findViewById(R.id.txtCategory);
            txtPrice=itemView.findViewById(R.id.txtPrice);
            txtStock=itemView.findViewById(R.id.txtStock);
            txtStatus=itemView.findViewById(R.id.txtStatus);

            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnDelete=itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                ArrayList<Product> filtered=new ArrayList<>();

                if(constraint==null || constraint.length()==0){

                    filtered.addAll(fullList);

                }else{

                    String search=constraint.toString().toLowerCase();

                    for(Product p:fullList){

                        if(p.getProductName().toLowerCase().contains(search)){

                            filtered.add(p);

                        }
                    }
                }

                FilterResults results=new FilterResults();
                results.values=filtered;
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