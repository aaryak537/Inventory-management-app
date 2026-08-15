package com.example.inventory;

public class Category {

    private String id;
    private String categoryName;
    private String description;
    private String status;

    // Used only for displaying the number of products
    // in the Category screen.
    // It does NOT have to be stored in Firebase.
    private int productCount;

    // Required empty constructor for Firebase
    public Category() {
    }

    // Constructor
    public Category(String id,
                    String categoryName,
                    String description,
                    String status) {

        this.id = id;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
        this.productCount = 0;
    }

    // ==============================
    // ID
    // ==============================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    // ==============================
    // CATEGORY NAME
    // ==============================

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


    // ==============================
    // DESCRIPTION
    // ==============================

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    // ==============================
    // STATUS
    // ==============================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // ==============================
    // PRODUCT COUNT
    // ==============================

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}