package com.example.inventory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryMovementAdapter
        extends RecyclerView.Adapter<InventoryMovementAdapter.ViewHolder> {

    private final List<InventoryMovement> movementList;

    public InventoryMovementAdapter(List<InventoryMovement> movementList) {
        this.movementList = movementList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventorymovement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        InventoryMovement movement = movementList.get(position);

        holder.tvType.setText(movement.getType());
        holder.tvProduct.setText(movement.getProductName());
        holder.tvDate.setText(movement.getDate());
        holder.tvParty.setText(movement.getPartyName());

        boolean purchase =
                "Purchase".equalsIgnoreCase(movement.getType());

        holder.tvQuantity.setText(
                (purchase ? "+" : "-")
                        + movement.getQuantity()
                        + " units"
        );

        holder.tvAmount.setText(
                StockUtils.formatINR(movement.getAmount())
        );

        holder.tvType.setTextColor(
                purchase
                        ? Color.rgb(22, 101, 52)
                        : Color.rgb(185, 28, 28)
        );
    }

    @Override
    public int getItemCount() {
        return movementList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvType;
        TextView tvProduct;
        TextView tvDate;
        TextView tvQuantity;
        TextView tvAmount;
        TextView tvParty;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvMovementType);
            tvProduct = itemView.findViewById(R.id.tvMovementProduct);
            tvDate = itemView.findViewById(R.id.tvMovementDate);
            tvQuantity = itemView.findViewById(R.id.tvMovementQuantity);
            tvAmount = itemView.findViewById(R.id.tvMovementAmount);
            tvParty = itemView.findViewById(R.id.tvMovementParty);
        }
    }
}
