package com.example.tuan_1;import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore; // Make sure this is imported
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import java.text.NumberFormat;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartContainer;
    private LinearLayout homeButton, profile;
    private TextView totalPriceTextView;

    // 1. Declare FirebaseFirestore instance variable
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        // 2. Initialize the FirebaseFirestore instance
        db = FirebaseFirestore.getInstance();

        cartContainer = findViewById(R.id.cartContainer);
        homeButton = findViewById(R.id.home);
        totalPriceTextView = findViewById(R.id.total_price_textview);
        profile = findViewById(R.id.profile);

        // Chuyen qua trang Toi
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        loadCartItems();

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(query -> {
                    cartContainer.removeAllViews();
                    totalPriceTextView.setText("Tổng thanh toán: 0đ");

                    for (QueryDocumentSnapshot doc : query) {

                        String docId = doc.getId();  // 🔥 Cần ID để xoá
                        String name = doc.getString("name");
                        String price = doc.getString("price");
                        String imageUrl = doc.getString("imageUrl");
                        Long quantity = doc.getLong("quantity");

                        LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                                .inflate(R.layout.cart_item, cartContainer, false);

                        ImageView img = item.findViewById(R.id.product_image);
                        TextView nameTv = item.findViewById(R.id.product_name);
                        TextView priceTv = item.findViewById(R.id.product_price);
                        TextView qtyTv = item.findViewById(R.id.product_quantity);
                        CheckBox checkbox = item.findViewById(R.id.checkbox);
                        Button btnRemove = item.findViewById(R.id.cancel_button);   // 🔥 nút hủy

                        Glide.with(this).load(imageUrl).into(img);

                        nameTv.setText(name);
                        priceTv.setText("Giá: " + price);
                        qtyTv.setText("Số lượng: " + (quantity != null ? quantity : 0));

                        checkbox.setOnCheckedChangeListener((btn, isChecked) -> updateTotalPrice());

                        // 🔥 Xử lý nút Hủy (xóa khỏi Firestore + UI)
                        btnRemove.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(a -> {
                                        cartContainer.removeView(item);  // 🔥 Xóa khỏi UI
                                        updateTotalPrice();              // Cập nhật lại tiền
                                    })
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                    });
                        });

                        cartContainer.addView(item);
                    }

                    updateTotalPrice();
                });
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
                    // Log the error for debugging
                    e.printStackTrace();
                }
            }
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Tổng thanh toán: " + nf.format(total) + "đ");
    }
}
