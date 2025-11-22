package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private int quantity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        ImageView imgProduct = findViewById(R.id.imgProduct);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPrice = findViewById(R.id.tvPrice);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);

        // Nhận dữ liệu từ Intent
        String name = getIntent().getStringExtra("product_name");
        int imageRes = getIntent().getIntExtra("product_image", 0);
        String price = getIntent().getStringExtra("product_price");

        // Hiển thị dữ liệu
        tvName.setText(name);
        imgProduct.setImageResource(imageRes);
        tvPrice.setText("Giá: " + price);

        // Xử lý thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            int selectedQuantity = quantity;

            Product product = new Product(name, price, imageRes, selectedQuantity);
            CartManager.getInstance().addToCart(product);

            Toast.makeText(this, "Đã thêm " + selectedQuantity + " sản phẩm!", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút back
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Xử lý logic của nút tăng giảm số lượng
        TextView tvQuantity;
        ImageView btnPlus, btnMinus;
        quantity = 1;

        tvQuantity = findViewById(R.id.tvQuantity);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);

        // Tăng số lượng
        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Giảm số lượng
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });


    }
}
