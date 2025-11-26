package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private Button btnAddProduct, btnManageUsers, btnManageComments, btnManageProducts;
    private ImageView btnBackAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageComments = findViewById(R.id.btnManageComments);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnBackAdmin = findViewById(R.id.btnBackAdmin);

        btnBackAdmin.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ProfileActivity.class));
            finish();
        });

        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AddProductActivity.class));
        });

        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, UserManagementActivity.class));
        });

        btnManageComments.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, CommentManagementActivity.class));
        });

        // 👉 Quản lý sản phẩm
        btnManageProducts.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ProductManagementActivity.class));
        });
    }
}
