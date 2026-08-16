package com.example.inventory;

public class Supplier {

    private String id;
    private String name;
    private String company;
    private String phone;
    private String email;

    // Required for Firebase
    public Supplier() {
    }

    public Supplier(String id,
                    String name,
                    String company,
                    String phone,
                    String email) {

        this.id = id;
        this.name = name;
        this.company = company;
        this.phone = phone;
        this.email = email;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}