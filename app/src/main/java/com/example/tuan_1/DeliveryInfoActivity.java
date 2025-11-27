package com.example.tuan_1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DeliveryInfoActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryInfoActivity";

    private EditText edtName, edtVoucher, edtNote;
    private TextView tvTotalPrice, tvVoucherInfo;
    private Button btnBack, btnConfirm, btnApplyVoucher;
    private LinearLayout selectedItemsContainer;

    private ArrayList<CartItem> selectedItems;
    private long originalTotalPrice;
    private long finalTotalPrice;
    private int discountPercent = 0;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_info);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        edtName = findViewById(R.id.edtName);
        edtVoucher = findViewById(R.id.edtVoucher);
        edtNote = findViewById(R.id.edtNote);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvVoucherInfo = findViewById(R.id.tvVoucherInfo);
        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        selectedItemsContainer = findViewById(R.id.selected_items_container);

        originalTotalPrice = getIntent().getLongExtra("totalPrice", 0L);
        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        if (selectedItems == null) selectedItems = new ArrayList<>();

        finalTotalPrice = originalTotalPrice;

        updateTotalPriceDisplay();
        displaySelectedItems();

        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        btnConfirm.setOnClickListener(v -> {
            if (edtName.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên người nhận!", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmPurchase();
        });
    }

    private void updateTotalPriceDisplay() {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng tiền: " + nf.format(finalTotalPrice) + "đ");
    }

    private void displaySelectedItems() {
        selectedItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (selectedItems.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Không có sản phẩm nào.");
            selectedItemsContainer.addView(empty);
            return;
        }

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        for (CartItem ci : selectedItems) {
            View row = inflater.inflate(android.R.layout.simple_list_item_2, selectedItemsContainer, false);
            TextView tv1 = row.findViewById(android.R.id.text1);
            TextView tv2 = row.findViewById(android.R.id.text2);

            tv1.setText(ci.getName() + " × " + ci.getBuyQuantity());
            tv2.setText("Giá: " + nf.format(ci.getPrice()) + "đ");

            selectedItemsContainer.addView(row);
        }
    }

    private void applyVoucher() {
        String code = edtVoucher.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        btnApplyVoucher.setEnabled(false);

        db.collection("vouchers")
                .whereEqualTo("name", code)
                .get()
                .addOnSuccessListener(snap -> {
                    btnApplyVoucher.setEnabled(true);

                    if (snap.isEmpty()) {
                        Toast.makeText(this, "Voucher không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    var doc = snap.getDocuments().get(0);

                    String startDateStr = doc.getString("startDate");
                    String endDateStr = doc.getString("endDate");
                    Number discountNum = doc.getLong("discount");

                    if (discountNum == null) discountNum = 0;
                    discountPercent = discountNum.intValue();

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                        sdf.setLenient(false);

                        // Normalize today's date to the beginning of the day
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        Date today = cal.getTime();

                        // Parse start and end dates
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);

                        if (today.before(startDate)) {
                            Toast.makeText(this, "Voucher chưa có hiệu lực", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (today.after(endDate)) {
                            Toast.makeText(this, "Voucher đã hết hạn", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Voucher is valid, apply discount
                        finalTotalPrice = originalTotalPrice - (originalTotalPrice * discountPercent / 100);
                        updateTotalPriceDisplay();
                        tvVoucherInfo.setText("Đã áp dụng giảm " + discountPercent + "%");
                        tvVoucherInfo.setVisibility(View.VISIBLE);

                        edtVoucher.setEnabled(false);
                        btnApplyVoucher.setEnabled(false);

                    } catch (ParseException | NullPointerException ex) {
                        Toast.makeText(this, "Ngày của voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnApplyVoucher.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmPurchase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnBack.setEnabled(false);

        String uid = user.getUid();
        WriteBatch batch = db.batch();

        for (CartItem ci : selectedItems) {
            DocumentReference ref = db.collection("users").document(uid).collection("cart").document(ci.getDocId());
            long newStock = ci.getQuantity() - ci.getBuyQuantity();
            if (newStock > 0) {
                batch.update(ref, "quantity", newStock);
            } else {
                batch.delete(ref);
            }
        }

        batch.commit()
                .addOnSuccessListener(a -> saveOrderHistory(uid))
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnBack.setEnabled(true);
                    Toast.makeText(this, "Lỗi giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveOrderHistory(String uid) {
        DocumentReference orderRef = db.collection("order_history").document();
        String orderId = orderRef.getId();

        ArrayList<Map<String, Object>> productList = new ArrayList<>();
        for (CartItem ci : selectedItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", ci.getName());
            map.put("imageUrl", ci.getImageUrl());
            map.put("price", ci.getPrice());
            map.put("buyQuantity", ci.getBuyQuantity());
            productList.add(map);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("status", "Đang xử lý");
        orderData.put("totalPrice", finalTotalPrice);
        orderData.put("userId", uid);
        orderData.put("products", productList);
        orderData.put("note", edtNote.getText().toString().trim());
        orderData.put("timestamp", Timestamp.now());

        orderRef.set(orderData)
                .addOnSuccessListener(a -> {
                    createNotification(uid, orderId);
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnBack.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotification(String uid, String orderId) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        StringBuilder msg = new StringBuilder();
        msg.append("Đơn hàng ").append(orderId).append(" đã được tạo.\n").append("Tổng: ").append(nf.format(finalTotalPrice)).append("đ\n").append("Sản phẩm:\n");

        for (CartItem ci : selectedItems) {
            msg.append("• ").append(ci.getName()).append(" × ").append(ci.getBuyQuantity()).append("\n");
        }

        Map<String, Object> notify = new HashMap<>();
        notify.put("title", "Đặt hàng thành công");
        notify.put("content", msg.toString());
        notify.put("timestamp", Timestamp.now());

        db.collection("users").document(uid).collection("notifications").add(notify).addOnSuccessListener(a -> Log.d(TAG, "Notification created"));
    }
}
