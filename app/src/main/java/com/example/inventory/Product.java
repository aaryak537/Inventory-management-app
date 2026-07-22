package com.example.inventory;

public class Product {

    private String productName;
    private String category;
    private int quantity;
    private String costPrice;
    private String sellingPrice;
    private String stock;
    private String brandName;
    private String description;
    private String imageUrl;

    // Required empty constructor for Firebase
    public Product() {
    }
    public Product(String productName, String category, int quantity,
                   String brandName, String costPrice, String sellingPrice,
                   String stock, String description, String imageUrl) {

        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.brandName=brandName;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
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
    public String getCostPrice() {
        return costPrice;
    }
    public String getSellingPrice() {
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
    public void setCostPrice(String costPrice) {
        this.costPrice = costPrice;
    }
    public void setSellingPrice(String sellingPrice) {
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