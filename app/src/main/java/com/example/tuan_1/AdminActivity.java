package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private Button btnAddProduct, btnManageUsers, btnManageComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageComments = findViewById(R.id.btnManageComments);

        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AddProductActivity.class));
        });

        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, UserManagementActivity.class));
        });

        btnManageComments.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, CommentManagementActivity.class));
        });
    }
}
