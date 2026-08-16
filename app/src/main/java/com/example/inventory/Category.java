package com.example.inventory;

public class Category {

    private String id;
    private String categoryName;
    private String description;
    private String status;
    private int productCount;

    public Category() {}

    public Category(String id, String categoryName, String description, String status) {

        this.id = id;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
        this.productCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}