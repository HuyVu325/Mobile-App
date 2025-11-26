package com.example.tuan_1;

import android.os.Bundle;
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

    private EditText edtName, edtVoucher, edtNote;
    private TextView tvTotalPrice;
    private Button btnBack, btnConfirm;
    private LinearLayout selectedItemsContainer;

    private ArrayList<CartItem> selectedItems;
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

        long total = getIntent().getLongExtra("totalPrice", 0);
        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng tiền: " + nf.format(total));

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

        // Lấy tên người nhận từ EditText
        String recipientName = edtName.getText().toString().trim();

        btnConfirm.setEnabled(false);
        btnBack.setEnabled(false);

        String uid = user.getUid();

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (CartItem item : selectedItems) {
            DocumentReference productRef = db.collection("users").document(uid).collection("cart").document(item.getDocId());
            tasks.add(productRef.get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(documentSnapshots -> {
            WriteBatch batch = db.batch();
            for (int i = 0; i < documentSnapshots.size(); i++) {
                DocumentSnapshot snapshot = (DocumentSnapshot) documentSnapshots.get(i);
                CartItem item = selectedItems.get(i);
                DocumentReference productRef = db.collection("users").document(uid).collection("cart").document(item.getDocId());

                if (snapshot.exists()) {
                    Long currentStockObj = snapshot.getLong("quantity");
                    long currentStock = (currentStockObj != null) ? currentStockObj : 0;
                    long newStock = currentStock - item.getBuyQuantity();

                    if (newStock <= 0) {
                        batch.delete(productRef);
                    } else {
                        batch.update(productRef, "quantity", newStock);
                    }
                }
            }
            // Truyền tên người nhận vào commitBatch
            commitBatch(batch, recipientName);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Không thể lấy thông tin giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnConfirm.setEnabled(true);
            btnBack.setEnabled(true);
        });
    }

    // Thêm tham số recipientName
    private void commitBatch(WriteBatch batch, String recipientName) {
        batch.commit().addOnSuccessListener(aVoid -> {
            String totalPriceString = tvTotalPrice.getText().toString();

            // Truyền cả recipientName vào hàm tạo thông báo
            createOrderSuccessNotification(selectedItems, totalPriceString, recipientName);

            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Cập nhật giỏ hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnConfirm.setEnabled(true);
            btnBack.setEnabled(true);
        });
    }

    /**
     * Tạo và lưu thông báo "Đặt hàng thành công" với chi tiết sản phẩm, giá, tổng tiền và tên người nhận.
     * @param purchasedItems Danh sách các sản phẩm đã mua.
     * @param totalPriceString Chuỗi hiển thị tổng số tiền.
     * @param recipientName Tên của người nhận hàng.
     */
    // Thêm tham số recipientName
    private void createOrderSuccessNotification(ArrayList<CartItem> purchasedItems, String totalPriceString, String recipientName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        String title = "Đặt hàng thành công";

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Xây dựng nội dung thông báo
        StringBuilder contentBuilder = new StringBuilder();
        // Thêm tên người nhận vào đầu tiên
        contentBuilder.append("Người nhận: ").append(recipientName).append("\n\n");
        contentBuilder.append("Đơn hàng của bạn đã được tiếp nhận, bao gồm:\n");

        for (CartItem item : purchasedItems) {
            contentBuilder
                    .append("• ")
                    .append(item.getName())
                    .append(" - ")
                    .append(nf.format(item.getPrice())) // Giả định có item.getPrice()
                    .append(" (SL: ")
                    .append(item.getBuyQuantity())
                    .append(")\n");
        }

        // Thêm dòng tổng tiền
        contentBuilder.append("\n");
        contentBuilder.append(totalPriceString);

        String content = contentBuilder.toString();

        Timestamp timestamp = new Timestamp(new Date());
        NotificationModel newNotification = new NotificationModel(title, content, timestamp);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(newNotification)
                .addOnSuccessListener(documentReference -> {
                    // Log success if needed
                })
                .addOnFailureListener(e -> {
                    // Log failure if needed
                });
    }
}
