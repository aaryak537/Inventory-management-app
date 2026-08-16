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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder>
        implements Filterable {

    private final Context context;


    private final ArrayList<Supplier> supplierList;


    private final ArrayList<Supplier> supplierListFull;




    public SupplierAdapter(
            Context context,
            ArrayList<Supplier> supplierList) {

        this.context = context;

        this.supplierList =
                new ArrayList<>(supplierList);

        this.supplierListFull =
                new ArrayList<>(supplierList);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_supplier,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }




    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Supplier supplier =
                supplierList.get(position);



        holder.txtSupplierName.setText(
                safeText(supplier.getName())
        );



        holder.txtCompany.setText(
                safeText(supplier.getCompany())
        );



        holder.txtPhone.setText(
                safeText(supplier.getPhone())
        );



        holder.txtEmail.setText(
                safeText(supplier.getEmail())
        );




        holder.btnMore.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            context,
                            holder.btnMore
                    );


            MenuInflater inflater =
                    popupMenu.getMenuInflater();


            inflater.inflate(
                    R.menu.supplier_menu,
                    popupMenu.getMenu()
            );


            popupMenu.setOnMenuItemClickListener(
                    item -> {




                        if (item.getItemId()
                                == R.id.menuEdit) {

                            Intent intent =
                                    new Intent(
                                            context,
                                            EditSupplierActivity.class
                                    );


                            intent.putExtra(
                                    "id",
                                    supplier.getId()
                            );


                            intent.putExtra(
                                    "name",
                                    supplier.getName()
                            );


                            intent.putExtra(
                                    "company",
                                    supplier.getCompany()
                            );


                            intent.putExtra(
                                    "phone",
                                    supplier.getPhone()
                            );


                            intent.putExtra(
                                    "email",
                                    supplier.getEmail()
                            );


                            context.startActivity(intent);

                            return true;
                        }




                        if (item.getItemId()
                                == R.id.menuDelete) {

                            deleteSupplier(
                                    supplier
                            );

                            return true;
                        }


                        return false;
                    }
            );


            popupMenu.show();
        });
    }


    private void deleteSupplier(
            Supplier supplier) {

        new AlertDialog.Builder(context)

                .setTitle("Delete Supplier")

                .setMessage(
                        "Are you sure you want to delete "
                                + safeText(
                                supplier.getName()
                        )
                                + "?"
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteFromFirebase(supplier)
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .show();
    }



    private void deleteFromFirebase(
            Supplier supplier) {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (user == null) {

            Toast.makeText(
                    context,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (supplier.getId() == null
                ||
                supplier.getId().trim().isEmpty()) {

            Toast.makeText(
                    context,
                    "Supplier ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        FirebaseDatabase
                .getInstance()
                .getReference("Suppliers")
                .child(user.getUid())
                .child(supplier.getId())
                .removeValue()

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            context,
                            "Supplier Deleted Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            context,
                            "Delete failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }




    @Override
    public int getItemCount() {

        return supplierList.size();
    }



    public void updateList(
            ArrayList<Supplier> list) {


        supplierList.clear();

        if (list != null) {

            supplierList.addAll(
                    new ArrayList<>(list)
            );
        }


        supplierListFull.clear();

        if (list != null) {

            supplierListFull.addAll(
                    new ArrayList<>(list)
            );
        }


        notifyDataSetChanged();
    }




    @Override
    public Filter getFilter() {

        return supplierFilter;
    }


    private final Filter supplierFilter =
            new Filter() {

                @Override
                protected FilterResults performFiltering(
                        CharSequence constraint) {

                    ArrayList<Supplier> filteredList =
                            new ArrayList<>();



                    if (constraint == null
                            ||
                            constraint.length() == 0) {

                        filteredList.addAll(
                                supplierListFull
                        );

                    } else {

                        String filterPattern =
                                constraint
                                        .toString()
                                        .toLowerCase()
                                        .trim();


                        for (Supplier supplier :
                                supplierListFull) {

                            String name =
                                    safeText(
                                            supplier.getName()
                                    ).toLowerCase();


                            String company =
                                    safeText(
                                            supplier.getCompany()
                                    ).toLowerCase();


                            String phone =
                                    safeText(
                                            supplier.getPhone()
                                    );


                            String email =
                                    safeText(
                                            supplier.getEmail()
                                    ).toLowerCase();


                            if (name.contains(
                                    filterPattern)

                                    ||

                                    company.contains(
                                            filterPattern)

                                    ||

                                    phone.contains(
                                            filterPattern)

                                    ||

                                    email.contains(
                                            filterPattern)) {

                                filteredList.add(
                                        supplier
                                );
                            }
                        }
                    }


                    FilterResults results =
                            new FilterResults();

                    results.values =
                            filteredList;

                    return results;
                }


                @Override
                protected void publishResults(
                        CharSequence constraint,
                        FilterResults results) {

                    supplierList.clear();


                    if (results.values != null) {

                        supplierList.addAll(
                                (ArrayList<Supplier>)
                                        results.values
                        );
                    }


                    notifyDataSetChanged();
                }
            };



    private String safeText(String value) {

        return value == null ? "" : value;
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgSupplier;

        TextView txtSupplierName;
        TextView txtCompany;
        TextView txtPhone;
        TextView txtEmail;

        ImageButton btnMore;


        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);


            imgSupplier =
                    itemView.findViewById(
                            R.id.imgSupplier
                    );


            txtSupplierName =
                    itemView.findViewById(
                            R.id.txtSupplierName
                    );


            txtCompany =
                    itemView.findViewById(
                            R.id.txtCompany
                    );


            txtPhone =
                    itemView.findViewById(
                            R.id.txtPhone
                    );


            txtEmail =
                    itemView.findViewById(
                            R.id.txtEmail
                    );


            btnMore =
                    itemView.findViewById(
                            R.id.btnMore
                    );
        }
    }
}