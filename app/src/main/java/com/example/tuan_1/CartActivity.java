package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class CartActivity extends AppCompatActivity {
    private LinearLayout homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        homeButton = findViewById(R.id.home);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
