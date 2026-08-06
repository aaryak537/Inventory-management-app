package com.example.inventory;

import com.google.firebase.database.Exclude;

public class Product {

    @Exclude
    private String productId;

    private String productName;
    private String category;
    private int quantity;
    private double costPrice;
    private double sellingPrice;
    private String stock;
    private String brandName;
    private String description;
    private String imageUrl;

    public Product() {
    }

    public Product(String productName, String category, int quantity,
                   String brandName, double costPrice, double sellingPrice,
                   String stock, String description, String imageUrl) {

        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.brandName = brandName;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Product(String productName, String category,
                   double sellingPrice, int quantity, boolean inStock) {

        this.productName = productName;
        this.category = category;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.stock = inStock ? "In Stock" : "Out of Stock";
        this.brandName = "";
        this.costPrice = 0;
        this.description = "";
        this.imageUrl = "";
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

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBrandName() {
        return brandName;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public String getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isInStock() {
        return stock != null && stock.equalsIgnoreCase("In Stock");
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}