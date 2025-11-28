package com.example.tuan_1;

import java.util.List;

public class OrderHistory {
    private String orderId;
    private String status;
    private List<Product> products;
    private double totalPrice;
    private String userId;
    private String cancelReason;

    public OrderHistory() {
        // Required for Firebase
    }

    public OrderHistory(String orderId, String status, List<Product> products, double totalPrice, String userId) {
        this.orderId = orderId;
        this.status = status;
        this.products = products;
        this.totalPrice = totalPrice;
        this.userId = userId;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public List<Product> getProducts() { return products; }
    public double getTotalPrice() { return totalPrice; }
    public String getUserId() { return userId; }
    public String getCancelReason() { return cancelReason; }
}
