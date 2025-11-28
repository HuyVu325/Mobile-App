package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartContainer;
    private LinearLayout homeButton, profile, notification;
    private TextView totalPriceTextView;
    private Button buyButton;

    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> deliveryLauncher;

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
        notification = findViewById(R.id.announcement);

        profile.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, ProfileActivity.class)));
        homeButton.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, HomeActivity.class)));
        notification.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        buyButton.setOnClickListener(v -> {
            ArrayList<CartItem> selectedItems = getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            long totalPayment = selectedItems.stream().mapToLong(item -> item.getPrice() * item.getBuyQuantity()).sum();
            Intent deli = new Intent(CartActivity.this, DeliveryInfoActivity.class);
            deli.putParcelableArrayListExtra("selectedItems", selectedItems);
            deli.putExtra("totalPrice", totalPayment);
            deliveryLauncher.launch(deli);
        });

        // Xử lý kết quả trả về từ DeliveryInfoActivity
        deliveryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        // Tải lại giỏ hàng để cập nhật giao diện
                        loadCartItems();
                    }
                }
        );

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

                    if (query.isEmpty()) return;

                    for (QueryDocumentSnapshot doc : query) {
                        String docId = doc.getId();
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("imageUrl");
                        Long stockQuantity = doc.getLong("quantity");

                        Object priceObject = doc.get("price");
                        long priceValue = 0;

                        if (priceObject instanceof Number) {
                            priceValue = ((Number) priceObject).longValue();
                        } else if (priceObject instanceof String) {
                            String priceStr = ((String) priceObject).replaceAll("[^\\d.].*", "");
                            try {
                                priceValue = (long) Double.parseDouble(priceStr);
                            } catch (NumberFormatException ignored) {}
                        }

                        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                        String priceDisplayString = nf.format(priceValue) + "đ";

                        LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                                .inflate(R.layout.cart_item, cartContainer, false);

                        item.setTag(R.id.TAG_PRICE, priceValue);
                        item.setTag(R.id.TAG_DOC_ID, docId);
                        item.setTag(R.id.TAG_IMAGE_URL, imageUrl);
                        item.setTag(R.id.TAG_STOCK_QUANTITY, stockQuantity);


                        ImageView img = item.findViewById(R.id.product_image);
                        TextView nameTv = item.findViewById(R.id.product_name);
                        TextView priceTv = item.findViewById(R.id.product_price);
                        TextView qtyTv = item.findViewById(R.id.product_quantity);
                        CheckBox checkbox = item.findViewById(R.id.checkbox);
                        Button btnRemove = item.findViewById(R.id.cancel_button);

                        ImageView btnMinus = item.findViewById(R.id.btnMinus);
                        ImageView btnPlus = item.findViewById(R.id.btnPlus);
                        TextView tvQuantity = item.findViewById(R.id.tvQuantity);

                        Glide.with(this).load(imageUrl).into(img);
                        nameTv.setText(name);
                        priceTv.setText("Giá: " + priceDisplayString);

                        int stock = stockQuantity != null ? stockQuantity.intValue() : 1;
                        qtyTv.setText("Số lượng: " + stock);

                        tvQuantity.setText("1");

                        checkbox.setOnCheckedChangeListener((btn, isChecked) -> updateTotalPrice());

                        btnPlus.setOnClickListener(v -> {
                            int buyQty = Integer.parseInt(tvQuantity.getText().toString());
                            if (buyQty < stock) {
                                buyQty++;
                                tvQuantity.setText(String.valueOf(buyQty));
                                if (checkbox.isChecked()) updateTotalPrice();
                            } else {
                                Toast.makeText(this, "Không thể vượt quá số lượng trong giỏ", Toast.LENGTH_SHORT).show();
                            }
                        });

                        btnMinus.setOnClickListener(v -> {
                            int buyQty = Integer.parseInt(tvQuantity.getText().toString());
                            if (buyQty > 1) {
                                buyQty--;
                                tvQuantity.setText(String.valueOf(buyQty));
                                if (checkbox.isChecked()) updateTotalPrice();
                            }
                        });

                        btnRemove.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(a -> {
                                        cartContainer.removeView(item);
                                        updateTotalPrice();
                                        Toast.makeText(CartActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
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
                long price = (long) item.getTag(R.id.TAG_PRICE);
                TextView tvQty = item.findViewById(R.id.tvQuantity);
                int qty = Integer.parseInt(tvQty.getText().toString());
                total += price * qty;
            }
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Tổng thanh toán: " + nf.format(total));
    }

    private ArrayList<CartItem> getSelectedItems() {
        ArrayList<CartItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < cartContainer.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) cartContainer.getChildAt(i);
            CheckBox checkbox = item.findViewById(R.id.checkbox);

            if (checkbox.isChecked()) {
                String docId = (String) item.getTag(R.id.TAG_DOC_ID);
                long price = (long) item.getTag(R.id.TAG_PRICE);
                String imageUrl = (String) item.getTag(R.id.TAG_IMAGE_URL);
                Long stockQuantityLong = (Long) item.getTag(R.id.TAG_STOCK_QUANTITY);
                int stockQuantity = stockQuantityLong != null ? stockQuantityLong.intValue() : 0;

                TextView nameTv = item.findViewById(R.id.product_name);
                TextView tvQty = item.findViewById(R.id.tvQuantity);

                String name = nameTv.getText().toString();
                int buyQty = Integer.parseInt(tvQty.getText().toString());

                selectedItems.add(new CartItem(name, imageUrl, price, buyQty, stockQuantity, docId));
            }
        }
        return selectedItems;
    }
}
