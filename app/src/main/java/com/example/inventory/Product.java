package com.example.inventory;

import com.google.firebase.database.Exclude;

public class Product {

    @Exclude
    private String productId;

    private String productName;

    // Category relationship
    private String categoryId;
    private String category;

    private int quantity;
    private double costPrice;
    private double sellingPrice;
    private String stock;
    private String brandName;
    private String description;
    private String imageUrl;

    // Required empty constructor for Firebase
    public Product() {
    }

    // Main constructor
    public Product(String productName,
                   String categoryId,
                   String category,
                   int quantity,
                   String brandName,
                   double costPrice,
                   double sellingPrice,
                   String stock,
                   String description,
                   String imageUrl) {

        this.productName = productName;
        this.categoryId = categoryId;
        this.category = category;
        this.quantity = quantity;
        this.brandName = brandName;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Existing constructor kept for compatibility
    public Product(String productName,
                   String category,
                   int quantity,
                   String brandName,
                   double costPrice,
                   double sellingPrice,
                   String stock,
                   String description,
                   String imageUrl) {

        this.productName = productName;
        this.category = category;
        this.categoryId = "";
        this.quantity = quantity;
        this.brandName = brandName;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Existing constructor kept for compatibility
    public Product(String productName,
                   String category,
                   double sellingPrice,
                   int quantity,
                   boolean inStock) {

        this.productName = productName;
        this.category = category;
        this.categoryId = "";
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.stock = inStock
                ? "In Stock"
                : "Out of Stock";

        this.brandName = "";
        this.costPrice = 0;
        this.description = "";
        this.imageUrl = "";
    }

    // Product ID
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    // Product Name
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Category ID
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    // Category Name
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Brand
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    // Cost Price
    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    // Selling Price
    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    // Stock
    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Image
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // In Stock
    public boolean isInStock() {
        return stock != null &&
                stock.equalsIgnoreCase("In Stock");
    }
}