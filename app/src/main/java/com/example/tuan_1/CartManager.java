package com.example.tuan_1;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Product> cartList = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product newProduct) {
        for (Product p : cartList) {
            if (p.getName().equals(newProduct.getName())) {
                // nếu trùng tên → cộng dồn số lượng
                p.setQuantity(p.getQuantity() + newProduct.getQuantity());
                return;
            }
        }
        // nếu chưa có → thêm mới
        cartList.add(newProduct);
    }

    public List<Product> getCartItems() {
        return cartList;
    }
}
