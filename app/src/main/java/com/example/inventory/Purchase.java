package com.example.inventory;

public class Purchase {

    private String purchaseId;
    private String productId;
    private String productName;

    private String supplierId;
    private String supplierName;

    private int quantity;
    private double purchasePrice;
    private double totalAmount;

    private String purchaseDate;
    private String invoiceNumber;
    private String notes;

    public Purchase() {

    }

    public Purchase(String purchaseId,
                    String productId,
                    String productName,
                    String supplierId,
                    String supplierName,
                    int quantity,
                    double purchasePrice,
                    double totalAmount,
                    String purchaseDate,
                    String invoiceNumber,
                    String notes) {

        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
        this.invoiceNumber = invoiceNumber;
        this.notes = notes;
    }


    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }


    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }


    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }


    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }


    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}