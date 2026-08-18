package com.example.inventory;

public class Sale {

    // ============================================================
    // VARIABLES
    // ============================================================

    private String saleId;
    private String productId;
    private String productName;
    private String productImageUrl;

    private int quantity;

    private double sellingPrice;
    private double totalAmount;

    private String customerName;
    private String paymentMethod;

    private String saleDate;
    private String saleTime;

    private long timestamp;


    // ============================================================
    // EMPTY CONSTRUCTOR
    // REQUIRED BY FIREBASE
    // ============================================================

    public Sale() {
    }


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public Sale(
            String saleId,
            String productId,
            String productName,
            String productImageUrl,
            int quantity,
            double sellingPrice,
            double totalAmount,
            String customerName,
            String paymentMethod,
            String saleDate,
            String saleTime,
            long timestamp
    ) {

        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
        this.saleTime = saleTime;
        this.timestamp = timestamp;
    }


    // ============================================================
    // SALE ID
    // ============================================================

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }


    // ============================================================
    // PRODUCT ID
    // ============================================================

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }


    // ============================================================
    // PRODUCT NAME
    // ============================================================

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    // ============================================================
    // PRODUCT IMAGE
    // ============================================================

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }


    // ============================================================
    // QUANTITY
    // ============================================================

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    // ============================================================
    // SELLING PRICE
    // ============================================================

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }


    // ============================================================
    // TOTAL AMOUNT
    // ============================================================

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }


    // ============================================================
    // CUSTOMER NAME
    // ============================================================

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    // ============================================================
    // PAYMENT METHOD
    // ============================================================

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    // ============================================================
    // SALE DATE
    // ============================================================

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }


    // ============================================================
    // SALE TIME
    // ============================================================

    public String getSaleTime() {
        return saleTime;
    }

    public void setSaleTime(String saleTime) {
        this.saleTime = saleTime;
    }


    // ============================================================
    // TIMESTAMP
    // ============================================================

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}