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

    LinearLayout cartContainer;
    LinearLayout homeButton;
    TextView totalPriceTextView; // hiển thị tổng tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        cartContainer = findViewById(R.id.cartContainer);
        homeButton = findViewById(R.id.home);
        totalPriceTextView = findViewById(R.id.total_price_textview);

        // Load dữ liệu giỏ hàng
        loadCartItems();

        // Nút quay về Home
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        cartContainer.removeAllViews();

        // reset tổng tiền ban đầu
        totalPriceTextView.setText("Tổng thanh toán: 0đ");

        for (Product product : CartManager.getInstance().getCartItems()) {

            // Inflate đúng layout cart_item.xml
            LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.cart_item, cartContainer, false);

            // LẤY ĐÚNG ID THEO XML
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            ImageView img = item.findViewById(R.id.product_image);
            TextView name = item.findViewById(R.id.product_name);
            TextView price = item.findViewById(R.id.product_price);
            Button cancel = item.findViewById(R.id.cancel_button);

            // Gán dữ liệu sản phẩm
            img.setImageResource(product.getImageRes());
            name.setText(product.getName());
            price.setText("Giá: " + product.getPrice());

            // Xử lý checkbox
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateTotalPrice();
            });

            // Xử lý nút Hủy
            cancel.setOnClickListener(v -> {
                // Xóa sản phẩm khỏi danh sách
                CartManager.getInstance().getCartItems().remove(product);

                // Xóa riêng item này khỏi layout
                cartContainer.removeView(item);

                // Cập nhật lại tổng tiền
                updateTotalPrice();
            });

            cartContainer.addView(item);
        }
    }

    // Hàm tính tổng tiền các sản phẩm đã tick
    private void updateTotalPrice() {
        int total = 0;

        for (int i = 0; i < cartContainer.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) cartContainer.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            TextView priceView = item.findViewById(R.id.product_price);

            if (checkbox.isChecked()) {
                String priceText = priceView.getText().toString()
                        .replace("Giá: ", "")
                        .replace(".", "")
                        .replace("đ", "")
                        .trim();

                try {
                    int price = Integer.parseInt(priceText);
                    total += price;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Tổng thanh toán: " + nf.format(total) + "đ");
    }
}
