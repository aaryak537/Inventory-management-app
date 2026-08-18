package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {

    private final Context context;
    private final List<Sale> saleList;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SaleAdapter(
            Context context,
            List<Sale> saleList
    ) {

        this.context = context;
        this.saleList = saleList;
    }


    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_sale,
                        parent,
                        false
                );

        return new SaleViewHolder(view);
    }


    // ============================================================
    // BIND DATA
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull SaleViewHolder holder,
            int position
    ) {

        Sale sale = saleList.get(position);

        if (sale == null) {
            return;
        }


        // --------------------------------------------------------
        // PRODUCT NAME
        // --------------------------------------------------------

        String productName = sale.getProductName();

        if (productName != null && !productName.trim().isEmpty()) {

            holder.tvProductName.setText(productName);

        } else {

            holder.tvProductName.setText("Unknown Product");
        }


        // --------------------------------------------------------
        // SALE DATE + TIME
        // --------------------------------------------------------

        String date = sale.getSaleDate();
        String time = sale.getSaleTime();

        if (date != null && !date.isEmpty()
                && time != null && !time.isEmpty()) {

            holder.tvSaleDate.setText(
                    date + " • " + time
            );

        } else if (date != null && !date.isEmpty()) {

            holder.tvSaleDate.setText(date);

        } else {

            holder.tvSaleDate.setText("Sale date unavailable");
        }


        // --------------------------------------------------------
        // QUANTITY
        // --------------------------------------------------------

        holder.tvQuantity.setText(
                "Qty: " + sale.getQuantity()
        );


        // --------------------------------------------------------
        // TOTAL AMOUNT
        // --------------------------------------------------------

        holder.tvTotalAmount.setText(
                "₹" + formatAmount(
                        sale.getTotalAmount()
                )
        );


        // --------------------------------------------------------
        // PAYMENT METHOD
        // --------------------------------------------------------

        String paymentMethod =
                sale.getPaymentMethod();

        if (paymentMethod != null
                && !paymentMethod.trim().isEmpty()) {

            holder.tvPaymentMethod.setText(
                    paymentMethod
            );

        } else {

            holder.tvPaymentMethod.setText(
                    "Payment"
            );
        }


        // --------------------------------------------------------
        // PRODUCT IMAGE
        // --------------------------------------------------------

        String imageUrl =
                sale.getProductImageUrl();


        if (imageUrl != null
                && !imageUrl.trim().isEmpty()) {

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_sales)
                    .error(R.drawable.ic_sales)
                    .centerCrop()
                    .into(holder.imgProduct);

        } else {

            holder.imgProduct.setImageResource(
                    R.drawable.ic_sales
            );
        }
    }


    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {
        return saleList.size();
    }


    // ============================================================
    // FORMAT AMOUNT
    // ============================================================

    private String formatAmount(double amount) {

        if (amount == (long) amount) {

            return String.format(
                    Locale.getDefault(),
                    "%d",
                    (long) amount
            );

        } else {

            return String.format(
                    Locale.getDefault(),
                    "%.2f",
                    amount
            );
        }
    }


    // ============================================================
    // VIEW HOLDER
    // ============================================================

    static class SaleViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgProduct;

        TextView tvProductName;
        TextView tvSaleDate;
        TextView tvQuantity;
        TextView tvTotalAmount;
        TextView tvPaymentMethod;


        SaleViewHolder(@NonNull View itemView) {

            super(itemView);

            imgProduct =
                    itemView.findViewById(
                            R.id.imgProduct
                    );

            tvProductName =
                    itemView.findViewById(
                            R.id.tvProductName
                    );

            tvSaleDate =
                    itemView.findViewById(
                            R.id.tvSaleDate
                    );

            tvQuantity =
                    itemView.findViewById(
                            R.id.tvQuantity
                    );

            tvTotalAmount =
                    itemView.findViewById(
                            R.id.tvTotalAmount
                    );

            tvPaymentMethod =
                    itemView.findViewById(
                            R.id.tvPaymentMethod
                    );
        }
    }
}