package com.example.tuan_1;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String price;
    private int imageRes;
    private int quantity;   // << thêm

    public Product(String name, String price, int imageRes) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.quantity = 1; // mặc định
    }

    // Thêm constructor có số lượng
    public Product(String name, String price, int imageRes, int quantity) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
