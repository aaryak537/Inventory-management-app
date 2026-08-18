package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class PurchaseAdapter
        extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    private final Context context;
    private final ArrayList<Purchase> purchaseList;

    public PurchaseAdapter(
            Context context,
            ArrayList<Purchase> purchaseList) {

        this.context = context;
        this.purchaseList = purchaseList;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_purchase,
                        parent,
                        false
                );

        return new PurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PurchaseViewHolder holder,
            int position) {

        Purchase purchase = purchaseList.get(position);

        // Product
        String productName = purchase.getProductName();

        if (productName == null ||
                productName.trim().isEmpty()) {

            productName = "Unknown Product";
        }

        holder.tvProductName.setText(productName);

        // Supplier
        String supplierName = purchase.getSupplierName();

        if (supplierName == null ||
                supplierName.trim().isEmpty()) {

            supplierName = "Unknown Supplier";
        }

        holder.tvSupplierName.setText(
                supplierName
        );

        // Quantity
        holder.tvQuantity.setText(
                purchase.getQuantity() + " units"
        );

        // Purchase price
        holder.tvPurchasePrice.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        purchase.getPurchasePrice()
                )
        );

        // Total amount
        holder.tvTotalAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₹%.2f",
                        purchase.getTotalAmount()
                )
        );

        // Date
        String date = purchase.getPurchaseDate();

        if (date == null || date.trim().isEmpty()) {
            date = "—";
        }

        holder.tvPurchaseDate.setText(date);

        // Invoice
        String invoice = purchase.getInvoiceNumber();

        if (invoice == null ||
                invoice.trim().isEmpty()) {

            holder.invoiceLayout.setVisibility(
                    View.GONE
            );

        } else {

            holder.invoiceLayout.setVisibility(
                    View.VISIBLE
            );

            holder.tvInvoiceNumber.setText(invoice);
        }

        // Notes
        String notes = purchase.getNotes();

        if (notes == null ||
                notes.trim().isEmpty()) {

            holder.tvNotes.setVisibility(
                    View.GONE
            );

        } else {

            holder.tvNotes.setVisibility(
                    View.VISIBLE
            );

            holder.tvNotes.setText(
                    "Notes: " + notes
            );
        }
    }

    @Override
    public int getItemCount() {
        return purchaseList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class PurchaseViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvProductName;
        TextView tvSupplierName;
        TextView tvTotalAmount;
        TextView tvQuantity;
        TextView tvPurchasePrice;
        TextView tvPurchaseDate;
        TextView tvInvoiceNumber;
        TextView tvNotes;

        LinearLayout invoiceLayout;

        public PurchaseViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvProductName =
                    itemView.findViewById(
                            R.id.tvProductName
                    );

            tvSupplierName =
                    itemView.findViewById(
                            R.id.tvSupplierName
                    );

            tvTotalAmount =
                    itemView.findViewById(
                            R.id.tvTotalAmount
                    );

            tvQuantity =
                    itemView.findViewById(
                            R.id.tvQuantity
                    );

            tvPurchasePrice =
                    itemView.findViewById(
                            R.id.tvPurchasePrice
                    );

            tvPurchaseDate =
                    itemView.findViewById(
                            R.id.tvPurchaseDate
                    );

            tvInvoiceNumber =
                    itemView.findViewById(
                            R.id.tvInvoiceNumber
                    );

            tvNotes =
                    itemView.findViewById(
                            R.id.tvNotes
                    );

            invoiceLayout =
                    itemView.findViewById(
                            R.id.invoiceLayout
                    );
        }
    }
}