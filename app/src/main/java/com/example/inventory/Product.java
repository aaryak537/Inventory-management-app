package com.example.inventory;

public class Product {
    private String productName;
    private int quantity;
    private int price;
    public Product(){}
    public Product(String productName,int quantity,int price){
        this.productName=productName;
        this.quantity=quantity;
        this.price=price;
    }
    public String getProductName(){
        return productName;
    }
    public int getQuantity(){
        return quantity;
    }
    public int getPrice(){
        return price;
    }
}
