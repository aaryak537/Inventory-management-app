package com.example.inventory;

public class InventoryMovement {

    private String type;
    private String productName;
    private String date;
    private int quantity;
    private double amount;
    private String partyName;
    private long sortTime;

    public InventoryMovement() {
    }

    public InventoryMovement(
            String type,
            String productName,
            String date,
            int quantity,
            double amount,
            String partyName,
            long sortTime
    ) {
        this.type = type;
        this.productName = productName;
        this.date = date;
        this.quantity = quantity;
        this.amount = amount;
        this.partyName = partyName;
        this.sortTime = sortTime;
    }

    public String getType() {
        return type;
    }

    public String getProductName() {
        return productName;
    }

    public String getDate() {
        return date;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public String getPartyName() {
        return partyName;
    }

    public long getSortTime() {
        return sortTime;
    }
}
