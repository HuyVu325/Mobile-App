package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartContainer;
    private LinearLayout homeButton;
    private TextView totalPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        cartContainer = findViewById(R.id.cartContainer);
        homeButton = findViewById(R.id.home);
        totalPriceTextView = findViewById(R.id.total_price_textview);

        loadCartItems();

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        cartContainer.removeAllViews();

        // đặt tổng ban đầu = 0
        totalPriceTextView.setText("Tổng thanh toán: 0đ");

        for (Product product : CartManager.getInstance().getCartItems()) {
            LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.cart_item, cartContainer, false);

            CheckBox checkbox = item.findViewById(R.id.checkbox);
            ImageView img = item.findViewById(R.id.product_image);
            TextView name = item.findViewById(R.id.product_name);
            TextView price = item.findViewById(R.id.product_price);
            TextView quantityText = item.findViewById(R.id.product_quantity);
            Button cancel = item.findViewById(R.id.cancel_button);

            img.setImageResource(product.getImageRes());
            name.setText(product.getName());

            // Hiển thị giá + số lượng
            price.setText("Giá: " + product.getPrice());
            quantityText.setText("Số lượng: " + product.getQuantity());

            // Checkbox: khi tick/uncheck → cập nhật tổng
            checkbox.setOnCheckedChangeListener((btn, isChecked) -> {
                updateTotalPrice();
            });

            // Hủy sản phẩm
            cancel.setOnClickListener(v -> {
                CartManager.getInstance().getCartItems().remove(product);
                cartContainer.removeView(item);
                updateTotalPrice();
            });

            cartContainer.addView(item);
        }
    }

    private void updateTotalPrice() {
        long total = 0;

        for (int i = 0; i < cartContainer.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) cartContainer.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            TextView priceView = item.findViewById(R.id.product_price);
            TextView qtyView = item.findViewById(R.id.product_quantity);

            if (checkbox.isChecked()) {
                // Lấy giá
                String priceText = priceView.getText().toString()
                        .replace("Giá: ", "")
                        .replace(".", "")
                        .replace("đ", "")
                        .trim();
                // Lấy số lượng
                String qtyText = qtyView.getText().toString()
                        .replace("Số lượng: ", "")
                        .trim();

                try {
                    long price = Long.parseLong(priceText);
                    int qty = Integer.parseInt(qtyText);
                    total += price * qty;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Tổng thanh toán: " + nf.format(total) + "đ");
    }
}
