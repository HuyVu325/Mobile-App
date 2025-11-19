package com.example.tuan_1;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String price;
    private int imageRes;

    public Product(String name, String price, int imageRes) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }
}
