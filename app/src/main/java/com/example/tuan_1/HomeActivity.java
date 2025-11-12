package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.example.tuan_1.GridAdapter;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout CartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        GridView gridView = findViewById(R.id.gridView);

        // Danh sách chữ
        String[] items = {"Apple", "Banana", "Cherry", "Date", "Grapes"};
        String[] prices = {"10.000đ", "8.000đ", "12.000đ", "9.000đ", "15.000đ"};

        // Danh sách hình (đặt file ảnh trong res/drawable/)
        int[] images = {
                R.drawable.apple,   // apple.png trong drawable
                R.drawable.banana,  // banana.png
                R.drawable.cherry,  // cherry.png
                R.drawable.date,    // date.png
                R.drawable.grapes   // grapes.png
        };

        // Tạo adapter và gắn vào GridView
        GridAdapter adapter = new GridAdapter(this, items, images, prices);
        gridView.setAdapter(adapter);

        // tạo kết nối với trang cart
        CartButton = findViewById(R.id.cart);

        CartButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // khi click vào một ảnh
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("product_name", items[position]);
            intent.putExtra("product_image", images[position]);
            intent.putExtra("product_price", prices[position]);
            startActivity(intent);
        });
    }
}