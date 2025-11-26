package com.example.tuan_1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeliveryInfoActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryInfoActivity";
    private EditText edtName, edtVoucher, edtNote;
    private TextView tvTotalPrice;
    private Button btnBack, btnConfirm;
    private LinearLayout selectedItemsContainer;

    private ArrayList<CartItem> selectedItems;
    private long totalPrice;
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
        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);
        selectedItemsContainer = findViewById(R.id.selected_items_container);

        totalPrice = getIntent().getLongExtra("totalPrice", 0);
        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng tiền: " + nf.format(totalPrice));

        displaySelectedItems();

        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên người nhận!", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmPurchase();
        });
    }

    private void displaySelectedItems() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        selectedItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CartItem item : selectedItems) {
            TextView itemView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, selectedItemsContainer, false);
            String itemText = String.format(Locale.US, "• %s (Số lượng: %d)", item.getName(), item.getBuyQuantity());
            itemView.setText(itemText);
            itemView.setTextSize(16);
            selectedItemsContainer.addView(itemView);
        }
    }

    private void confirmPurchase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng hoặc sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        String recipientName = edtName.getText().toString().trim();
        String voucherCode = edtVoucher.getText().toString().trim();
        String shipperNote = edtNote.getText().toString().trim();

        btnConfirm.setEnabled(false);
        btnBack.setEnabled(false);

        String uid = user.getUid();

        WriteBatch batch = db.batch();
        for (CartItem item : selectedItems) {
            DocumentReference cartItemRef = db.collection("users").document(uid).collection("cart").document(item.getDocId());
            // This logic is simplified as we assume the cart activity passed the correct buyQuantity
            // A more robust solution would re-read the document before updating.
            long currentQuantity = item.getQuantity(); // Assuming CartItem holds the total quantity
            long newQuantity = currentQuantity - item.getBuyQuantity();
            if (newQuantity > 0) {
                batch.update(cartItemRef, "quantity", newQuantity);
            } else {
                batch.delete(cartItemRef);
            }
        }

        commitBatch(batch, recipientName, voucherCode, shipperNote);
    }

    private void commitBatch(WriteBatch batch, String recipientName, String voucherCode, String shipperNote) {
        batch.commit().addOnSuccessListener(aVoid -> {
            createOrderSuccessNotification(selectedItems, totalPrice, recipientName, voucherCode, shipperNote);
            saveOrderToHistory(selectedItems, totalPrice, recipientName, voucherCode, shipperNote);

            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Cập nhật giỏ hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnConfirm.setEnabled(true);
            btnBack.setEnabled(true);
        });
    }

    private void saveOrderToHistory(ArrayList<CartItem> purchasedItems, long totalPrice, String recipientName, String voucherCode, String shipperNote) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();

        DocumentReference orderRef = db.collection("order_history").document();
        String orderId = orderRef.getId();

        List<Product> products = new ArrayList<>();
        for (CartItem item : purchasedItems) {
            products.add(new Product(item.getDocId(), item.getName(), item.getBuyQuantity(), item.getPrice(), item.getImageUrl()));
        }

        String initialStatus = "Đang xử lý";

        OrderHistory newOrder = new OrderHistory(orderId, initialStatus, products, totalPrice, userId);

        orderRef.set(newOrder)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order history saved successfully for orderId: " + orderId))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving order history", e));
    }


    private void createOrderSuccessNotification(ArrayList<CartItem> purchasedItems, long totalPrice, String recipientName, String voucherCode, String shipperNote) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        String title = "Đặt hàng thành công";

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("Người nhận: ").append(recipientName).append("\n\n");
        contentBuilder.append("Đơn hàng của bạn đã được tiếp nhận, bao gồm:\n");

        for (CartItem item : purchasedItems) {
            contentBuilder
                    .append("• ")
                    .append(item.getName())
                    .append(" - ")
                    .append(nf.format(item.getPrice()))
                    .append(" (SL: ")
                    .append(item.getBuyQuantity())
                    .append(")\n");
        }
        contentBuilder.append("\n");

        if (voucherCode != null && !voucherCode.isEmpty()) {
            contentBuilder.append("Mã giảm giá: ").append(voucherCode).append("\n");
        }
        if (shipperNote != null && !shipperNote.isEmpty()) {
            contentBuilder.append("Ghi chú: ").append(shipperNote).append("\n");
        }

        contentBuilder.append("Tổng tiền: ").append(nf.format(totalPrice));

        String content = contentBuilder.toString();
        Timestamp timestamp = new Timestamp(new Date());
        NotificationModel newNotification = new NotificationModel(title, content, timestamp);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(newNotification)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notification sent successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Error sending notification", e));
    }
}
