package com.example.tuan_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter<OrderHistory, HistoryViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null)
            ivBack.setOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        Query query = db.collection("order_history")
                .whereEqualTo("userId", userId)
                .orderBy("orderId", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<OrderHistory> options =
                new FirestoreRecyclerOptions.Builder<OrderHistory>()
                        .setQuery(query, OrderHistory.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new FirestoreRecyclerAdapter<OrderHistory, HistoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HistoryViewHolder holder, int position, @NonNull OrderHistory model) {
                holder.bind(model);
            }

            @NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
                return new HistoryViewHolder(v);
            }
        };

        rvHistory.setAdapter(adapter);
    }

    private void showCancelReasonDialog(OrderHistory order) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_reason, null);
        final EditText edt = dialogView.findViewById(R.id.edtCancelReason);

        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (d, id) -> {
                    String rs = edt.getText().toString().trim();
                    if (rs.isEmpty()) {
                        Toast.makeText(this, "Bạn chưa nhập lý do", Toast.LENGTH_SHORT).show();
                    } else {
                        cancelOrder(order, rs);
                    }
                })
                .setNegativeButton("Thoát", null)
                .show();
    }

    private void cancelOrder(OrderHistory order, String reason) {
        if (order.getOrderId() == null) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Đã hủy");
        updates.put("cancelReason", reason);

        db.collection("order_history")
                .document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                    // Tải lại Activity
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog(OrderHistory order) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn đơn hàng này khỏi lịch sử không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteOrder(order))
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteOrder(OrderHistory order) {
        if (order.getOrderId() == null) {
            Toast.makeText(this, "Không thể xóa đơn hàng này!", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("order_history").document(order.getOrderId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HistoryActivity.this, "Đã xóa lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                    // Tải lại Activity
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                })
                .addOnFailureListener(e -> Toast.makeText(HistoryActivity.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderId, tvOrderStatus, tvTotalPrice, tvCancelReason;
        LinearLayout layoutProducts;
        Button btnCancel;
        ImageButton btnDelete;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            layoutProducts = itemView.findViewById(R.id.layoutProducts);
            tvCancelReason = itemView.findViewById(R.id.tvCancelReason);
            btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            btnDelete = itemView.findViewById(R.id.btnDeleteHistory);
        }

        void bind(OrderHistory o) {

            tvOrderId.setText("ID: " + o.getOrderId());
            tvOrderStatus.setText("Tình trạng: " + o.getStatus());
            tvTotalPrice.setText("Tổng giá: " + String.format("%,.0fđ", o.getTotalPrice()));

            layoutProducts.removeAllViews();
            if (o.getProducts() != null) {
                for (Product p : o.getProducts()) {
                    TextView t = new TextView(itemView.getContext());
                    t.setText("- " + p.getName() + " (SL: " + p.getBuyQuantity() + ")");
                    layoutProducts.addView(t);
                }
            }

            if ("Đang xử lý".equals(o.getStatus())) {
                btnCancel.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.GONE);
                tvCancelReason.setVisibility(View.GONE);

                btnCancel.setOnClickListener(v -> showCancelReasonDialog(o));

            } else if ("Đã hủy".equals(o.getStatus())) {
                btnCancel.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
                tvCancelReason.setVisibility(View.VISIBLE);
                tvCancelReason.setText("Lý do: " + o.getCancelReason());

                btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(o));

            } else {
                btnCancel.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                tvCancelReason.setVisibility(View.GONE);
            }
        }


    }
}