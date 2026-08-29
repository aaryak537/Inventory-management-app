package com.example.inventory;

import com.google.firebase.database.Exclude;

public class Product {

    private String productId;

    private String productName;
    private String categoryId;
    private String category;

    private int quantity;

    private String brandName;

    private double costPrice;
    private double sellingPrice;

    private String stock;

    private String description;
    private String imageUrl;






    public Product() {
    }






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

        this.stock =
                getCalculatedStockStatus(quantity);

        this.brandName = "";
        this.costPrice = 0.0;
        this.description = "";
        this.imageUrl = "";
    }






    @Exclude
    public String getProductId() {

        return productId;
    }


    @Exclude
    public void setProductId(
            String productId
    ) {

        this.productId = productId;
    }






    public String getProductName() {

        return productName;
    }


    public void setProductName(
            Object productName
    ) {

        this.productName =
                productName == null
                        ? ""
                        : String.valueOf(productName);
    }






    public String getCategoryId() {

        return categoryId;
    }


    public void setCategoryId(
            Object categoryId
    ) {

        this.categoryId =
                categoryId == null
                        ? ""
                        : String.valueOf(categoryId);
    }






    public String getCategory() {

        return category;
    }


    public void setCategory(
            Object category
    ) {

        this.category =
                category == null
                        ? ""
                        : String.valueOf(category);
    }






    public int getQuantity() {

        return quantity;
    }


    public void setQuantity(
            Object quantity
    ) {

        this.quantity =
                convertToInt(quantity);
    }






    public String getBrandName() {

        return brandName;
    }


    public void setBrandName(
            Object brandName
    ) {

        this.brandName =
                brandName == null
                        ? ""
                        : String.valueOf(brandName);
    }






    public double getCostPrice() {

        return costPrice;
    }


    public void setCostPrice(
            Object costPrice
    ) {

        this.costPrice =
                convertToDouble(costPrice);
    }






    public double getSellingPrice() {

        return sellingPrice;
    }


    public void setSellingPrice(
            Object sellingPrice
    ) {

        this.sellingPrice =
                convertToDouble(sellingPrice);
    }






    public String getStock() {

        return stock;
    }


    public void setStock(
            Object stock
    ) {

        this.stock =
                stock == null
                        ? ""
                        : String.valueOf(stock);
    }






    public String getDescription() {

        return description;
    }


    public void setDescription(
            Object description
    ) {

        this.description =
                description == null
                        ? ""
                        : String.valueOf(description);
    }






    public String getImageUrl() {

        return imageUrl;
    }


    public void setImageUrl(
            Object imageUrl
    ) {

        this.imageUrl =
                imageUrl == null
                        ? ""
                        : String.valueOf(imageUrl);
    }






    @Exclude
    public String getStockStatus() {

        return getCalculatedStockStatus(
                getEffectiveQuantity()
        );
    }


    private static String getCalculatedStockStatus(
            int quantity
    ) {

        return StockUtils.getStockStatus(quantity);
    }








    @Exclude
    public boolean isInStock() {

        return getEffectiveQuantity() > 0;
    }










    @Exclude
    public int getEffectiveQuantity() {

        if (quantity > 0) {
            return quantity;
        }

        if (stock != null) {

            try {
                double numericStock =
                        Double.parseDouble(stock.trim());

                if (numericStock >= 0) {
                    return (int) numericStock;
                }

            } catch (Exception ignored) {

            }
        }

        return 0;
    }






    private static int convertToInt(
            Object value
    ) {

        if (value == null) {
            return 0;
        }


        if (value instanceof Number) {

            return ((Number) value).intValue();
        }


        try {

            return Integer.parseInt(
                    String.valueOf(value).trim()
            );

        } catch (Exception e) {

            try {

                return (int) Double.parseDouble(
                        String.valueOf(value).trim()
                );

            } catch (Exception ignored) {

                return 0;
            }
        }
    }






    private static double convertToDouble(
            Object value
    ) {

        if (value == null) {
            return 0.0;
        }


        if (value instanceof Number) {

            return ((Number) value).doubleValue();
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