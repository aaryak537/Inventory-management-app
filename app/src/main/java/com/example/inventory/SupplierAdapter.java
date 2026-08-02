package com.example.inventory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder>
        implements Filterable {

    Context context;
    ArrayList<Supplier> supplierList;
    ArrayList<Supplier> supplierListFull;

    public SupplierAdapter(Context context, ArrayList<Supplier> supplierList) {

        this.context = context;
        this.supplierList = supplierList;
        this.supplierListFull = new ArrayList<>(supplierList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_supplier, parent,
                        false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Supplier supplier = supplierList.get(position);

        holder.txtSupplierName.setText(supplier.getName());
        holder.txtCompany.setText(supplier.getCompany());
        holder.txtPhone.setText(supplier.getPhone());
        holder.txtEmail.setText(supplier.getEmail());

        holder.btnMore.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);

            MenuInflater inflater = popupMenu.getMenuInflater();
          //  inflater.inflate(R.menu.supplier_menu,
                //    popupMenu.getMenu());

        //    popupMenu.setOnMenuItemClickListener(item -> {

            //    if (item.getItemId() == R.id.menuEdit) {

               //     Intent intent = new Intent(context,
                    //        EditSupplierActivity.class);

                //    intent.putExtra("id",
                //            supplier.getId());

                //    intent.putExtra("name",
               //             supplier.getName());

                  //  intent.putExtra("company",
                //   supplier.getCompany());

                   // intent.putExtra("phone",
                   //         supplier.getPhone());

                 //   intent.putExtra("email",
                 //           supplier.getEmail());

                //    context.startActivity(intent);

                  //  return true;

              //  } else if (item.getItemId() == R.id.menuDelete) {

                   // deleteSupplier(supplier);

                 //   return true;
              //  }

            //   return false;
         //   });
           popupMenu.show();
        });
    }
    private void deleteSupplier(Supplier supplier) {

        new AlertDialog.Builder(context)
                .setTitle("Delete Supplier")
                .setMessage("Are you sure you want to delete this supplier?")
                .setPositiveButton("Delete",
                        (dialog, which) -> {

                            FirebaseDatabase.getInstance()
                                    .getReference("Suppliers")
                                    .child(supplier.getId())
                                    .removeValue();

                            Toast.makeText(context, "Supplier Deleted",
                                    Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return supplierList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgSupplier;
        TextView txtSupplierName, txtCompany, txtPhone, txtEmail;
        ImageButton btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSupplier = itemView.findViewById(R.id.imgSupplier);

            txtSupplierName = itemView.findViewById(R.id.txtSupplierName);

            txtCompany = itemView.findViewById(R.id.txtCompany);

            txtPhone = itemView.findViewById(R.id.txtPhone);

            txtEmail = itemView.findViewById(R.id.txtEmail);

            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    @Override
    public Filter getFilter() {
        return supplierFilter;
    }

    private final Filter supplierFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Supplier> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {

                filteredList.addAll(supplierListFull);

            } else {
                String filterPattern = constraint.toString()
                                .toLowerCase()
                                .trim();

                for (Supplier supplier : supplierListFull) {

                    if (supplier.getName()
                            .toLowerCase()
                            .contains(filterPattern)
                            ||
                            supplier.getCompany()
                                    .toLowerCase()
                                    .contains(filterPattern)
                            ||
                            supplier.getPhone()
                                    .contains(filterPattern)
                            ||
                            supplier.getEmail()
                                    .toLowerCase()
                                    .contains(filterPattern)) {

                        filteredList.add(supplier);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            supplierList.clear();
            supplierList.addAll(
                    (ArrayList<Supplier>) results.values);
            notifyDataSetChanged();
        }
    };
    public void updateList(ArrayList<Supplier> list) {

        supplierList.clear();
        supplierList.addAll(list);

        supplierListFull.clear();
        supplierListFull.addAll(list);

        notifyDataSetChanged();
    }
}