package com.example.inventory;

public class User {

    public String name;
    public String email;
    public String phone;

    public User() {
        // Required for Firebase
    }

    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}