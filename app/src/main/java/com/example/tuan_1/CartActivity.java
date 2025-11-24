package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartContainer;
    private LinearLayout homeButton, profile;
    private TextView totalPriceTextView;
    private Button buyButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        db = FirebaseFirestore.getInstance();

        cartContainer = findViewById(R.id.cartContainer);
        homeButton = findViewById(R.id.home);
        totalPriceTextView = findViewById(R.id.total_price_textview);
        profile = findViewById(R.id.profile);
        buyButton = findViewById(R.id.buy_button);

        profile.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, ProfileActivity.class)));
        homeButton.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, HomeActivity.class)));

        buyButton.setOnClickListener(v -> openPaymentMethodDialog());

        loadCartItems();
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

                    if (query.isEmpty()) {
                        return;
                    }

                    for (QueryDocumentSnapshot doc : query) {
                        String docId = doc.getId();
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("imageUrl");
                        Long quantity = doc.getLong("quantity");

                        // Lấy giá trị dưới dạng Object để xử lý linh hoạt
                        Object priceObject = doc.get("price");
                        long priceValue = 0;
                        String priceDisplayString;

                        if (priceObject instanceof Number) {
                            // Nếu giá là số (ví dụ: 5000.0), chuyển về long
                            priceValue = ((Number) priceObject).longValue();
                        } else if (priceObject instanceof String) {
                            // Nếu giá là chuỗi (ví dụ: "5000.0" hoặc "5000"), làm sạch và chuyển đổi
                            String priceStr = (String) priceObject;
                            // Loại bỏ mọi thứ không phải là số ở đầu chuỗi
                            priceStr = priceStr.replaceAll("[^\\d.].*", "");
                            try {
                                priceValue = (long) Double.parseDouble(priceStr);
                            } catch (NumberFormatException e) {
                                priceValue = 0;
                            }
                        }

                        // Định dạng lại chuỗi giá để hiển thị cho đẹp, không có .0
                        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                        priceDisplayString = nf.format(priceValue) + "đ";

                        LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                                .inflate(R.layout.cart_item, cartContainer, false);

                        // Lưu giá trị SỐ NGUYÊN (long) vào tag để tính toán
                        item.setTag(priceValue);

                        ImageView img = item.findViewById(R.id.product_image);
                        TextView nameTv = item.findViewById(R.id.product_name);
                        TextView priceTv = item.findViewById(R.id.product_price);
                        TextView qtyTv = item.findViewById(R.id.product_quantity);
                        CheckBox checkbox = item.findViewById(R.id.checkbox);
                        Button btnRemove = item.findViewById(R.id.cancel_button);

                        Glide.with(this).load(imageUrl).into(img);

                        nameTv.setText(name);
                        // HIỂN THỊ GIÁ ĐÃ ĐỊNH DẠNG LẠI, KHÔNG CÓ .0
                        priceTv.setText("Giá: " + priceDisplayString);
                        qtyTv.setText("Số lượng: " + (quantity != null ? quantity : 0));

                        checkbox.setOnCheckedChangeListener((btn, isChecked) -> updateTotalPrice());

                        btnRemove.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(a -> {
                                        cartContainer.removeView(item);
                                        updateTotalPrice();
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
            if (checkbox.isChecked()) {
                try {
                    // Lấy giá trị long đã lưu từ tag
                    long price = (long) item.getTag();
                    TextView qtyView = item.findViewById(R.id.product_quantity);
                    String qtyText = qtyView.getText().toString().replaceAll("[^\\d]", "");
                    int qty = Integer.parseInt(qtyText);
                    total += price * qty;
                } catch (Exception e) {
                    e.printStackTrace(); // In lỗi ra để debug nếu có
                }
            }
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Tổng thanh toán: " + nf.format(total));
    }

    private long getTotalPaymentValue() {
        long total = 0;
        for (int i = 0; i < cartContainer.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) cartContainer.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);
            if (checkbox.isChecked()) {
                try {
                    long price = (long) item.getTag();
                    TextView qtyView = item.findViewById(R.id.product_quantity);
                    String qtyText = qtyView.getText().toString().replaceAll("[^\\d]", "");
                    int qty = Integer.parseInt(qtyText);
                    total += price * qty;
                } catch (Exception ignored) {}
            }
        }
        return total;
    }

    private void openPaymentMethodDialog() {
        long totalPayment = getTotalPaymentValue();
        if (totalPayment <= 0) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn phương thức thanh toán");
        String[] methods = {"Thanh toán bằng QR", "Thanh toán khi nhận hàng"};
        builder.setItems(methods, (dialog, which) -> {
            if (which == 0) {
                Intent qr = new Intent(this, QRPaymentActivity.class);
                qr.putExtra("totalPrice", totalPayment);
                startActivity(qr);
            } else {
                Intent deli = new Intent(this, DeliveryInfoActivity.class);
                deli.putExtra("totalPrice", totalPayment);
                startActivity(deli);
            }
        });
        builder.show();
    }
}
