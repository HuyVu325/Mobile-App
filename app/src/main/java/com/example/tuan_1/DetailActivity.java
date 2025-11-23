package com.example.tuan_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgProduct, btnBack, btnMinus, btnPlus;
    private TextView tvName, tvPrice, tvQuantity;
    private Button btnAddToCart;

    private int quantity = 1;
    private String name, price, imageUrl, desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        imgProduct = findViewById(R.id.imgProduct);
        btnBack = findViewById(R.id.btnBack);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        name = getIntent().getStringExtra("product_name");
        price = getIntent().getStringExtra("product_price");
        imageUrl = getIntent().getStringExtra("product_image");
        desc = getIntent().getStringExtra("product_desc"); // có thể dùng sau

        tvName.setText(name != null ? name : "");
        tvPrice.setText(price != null ? price : "");

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.shop)
                .error(R.drawable.shop)
                .into(imgProduct);

        tvQuantity.setText(String.valueOf(quantity));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Đã thêm " + quantity + " " + name + " vào giỏ (demo)",
                    Toast.LENGTH_SHORT).show();
            // sau này bạn có thể lưu giỏ vào Firestore hoặc SQLite
        });
    }
}
