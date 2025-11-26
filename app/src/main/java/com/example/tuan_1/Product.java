package com.example.tuan_1;

public class Product {
    private String id;
    private String name;
    private int quantity;
    private double price;
    private String imageUrl;

    public Product() {
        // Required for Firebase
    }

    public Product(String id, String name, int quantity, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
