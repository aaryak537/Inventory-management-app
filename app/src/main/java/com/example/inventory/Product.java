package com.example.inventory;

import com.google.firebase.database.Exclude;

public class Product {

    // ============================================================
    // PRODUCT ID
    // ============================================================

    @Exclude
    private String productId;

    // ============================================================
    // PRODUCT DETAILS
    // ============================================================

    private String productName;

    private String categoryId;
    private String category;

    private int quantity;

    private double costPrice;
    private double sellingPrice;

    /*
     * IMPORTANT:
     * Keep stock as String internally, but accept ANY Firebase
     * value through setStock(Object).
     *
     * This prevents:
     * Failed to convert value of type java.lang.Long to String
     */
    private String stock;

    private String brandName;
    private String description;
    private String imageUrl;


    // ============================================================
    // EMPTY CONSTRUCTOR
    // REQUIRED BY FIREBASE
    // ============================================================

    public Product() {
    }


    // ============================================================
    // MAIN CONSTRUCTOR
    // ============================================================

    public Product(
            String productName,
            String categoryId,
            String category,
            int quantity,
            String brandName,
            double costPrice,
            double sellingPrice,
            String stock,
            String description,
            String imageUrl
    ) {

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


    // ============================================================
    // COMPATIBILITY CONSTRUCTOR
    // ============================================================

    public Product(
            String productName,
            String category,
            int quantity,
            String brandName,
            double costPrice,
            double sellingPrice,
            String stock,
            String description,
            String imageUrl
    ) {

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


    // ============================================================
    // COMPATIBILITY CONSTRUCTOR
    // ============================================================

    public Product(
            String productName,
            String category,
            double sellingPrice,
            int quantity,
            boolean inStock
    ) {

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


    // ============================================================
    // PRODUCT ID
    // ============================================================

    @Exclude
    public String getProductId() {
        return productId;
    }

    @Exclude
    public void setProductId(String productId) {
        this.productId = productId;
    }


    // ============================================================
    // PRODUCT NAME
    // ============================================================

    public String getProductName() {
        return productName;
    }

    public void setProductName(Object productName) {

        this.productName =
                productName == null
                        ? ""
                        : String.valueOf(productName);
    }


    // ============================================================
    // CATEGORY ID
    // ============================================================

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Object categoryId) {

        this.categoryId =
                categoryId == null
                        ? ""
                        : String.valueOf(categoryId);
    }


    // ============================================================
    // CATEGORY
    // ============================================================

    public String getCategory() {
        return category;
    }

    public void setCategory(Object category) {

        this.category =
                category == null
                        ? ""
                        : String.valueOf(category);
    }


    // ============================================================
    // QUANTITY
    // ============================================================

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(Object quantity) {

        this.quantity = convertToInt(quantity);
    }


    // ============================================================
    // BRAND NAME
    // ============================================================

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(Object brandName) {

        this.brandName =
                brandName == null
                        ? ""
                        : String.valueOf(brandName);
    }


    // ============================================================
    // COST PRICE
    // ============================================================

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Object costPrice) {

        this.costPrice =
                convertToDouble(costPrice);
    }


    // ============================================================
    // SELLING PRICE
    // ============================================================

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Object sellingPrice) {

        this.sellingPrice =
                convertToDouble(sellingPrice);
    }


    // ============================================================
    // STOCK
    // ============================================================

    public String getStock() {
        return stock;
    }

    /*
     * VERY IMPORTANT FIX
     *
     * Firebase may contain:
     *
     * "stock": "In Stock"
     *
     * OR
     *
     * "stock": 10
     *
     * OR
     *
     * "stock": 0
     *
     * Object accepts all of these safely.
     */
    public void setStock(Object stock) {

        if (stock == null) {

            this.stock = "";

        } else {

            this.stock =
                    String.valueOf(stock);
        }
    }


    // ============================================================
    // DESCRIPTION
    // ============================================================

    public String getDescription() {
        return description;
    }

    public void setDescription(Object description) {

        this.description =
                description == null
                        ? ""
                        : String.valueOf(description);
    }


    // ============================================================
    // IMAGE URL
    // ============================================================

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Object imageUrl) {

        this.imageUrl =
                imageUrl == null
                        ? ""
                        : String.valueOf(imageUrl);
    }


    // ============================================================
    // IN STOCK
    // ============================================================

    @Exclude
    public boolean isInStock() {

        if (stock == null) {
            return quantity > 0;
        }

        String value =
                stock.trim();

        /*
         * Handle normal text values
         */
        if (value.equalsIgnoreCase("In Stock")
                || value.equalsIgnoreCase("Available")
                || value.equalsIgnoreCase("true")) {

            return true;
        }

        if (value.equalsIgnoreCase("Out of Stock")
                || value.equalsIgnoreCase("Low Stock")
                || value.equalsIgnoreCase("false")) {

            return false;
        }

        /*
         * Handle old numeric stock values
         */
        try {

            double numericStock =
                    Double.parseDouble(value);

            return numericStock > 0;

        } catch (NumberFormatException ignored) {

            /*
             * If stock is not usable, fall back to quantity.
             */
            return quantity > 0;
        }
    }


    // ============================================================
    // SAFE INTEGER CONVERSION
    // ============================================================

    private static int convertToInt(Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Double) {
            return ((Double) value).intValue();
        }

        if (value instanceof Float) {
            return ((Float) value).intValue();
        }

        try {

            return Integer.parseInt(
                    String.valueOf(value).trim()
            );

        } catch (Exception e) {

            return 0;
        }
    }


    // ============================================================
    // SAFE DOUBLE CONVERSION
    // ============================================================

    private static double convertToDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }

        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }

        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }

        try {

            return Double.parseDouble(
                    String.valueOf(value).trim()
            );

        } catch (Exception e) {

            return 0.0;
        }
    }
}