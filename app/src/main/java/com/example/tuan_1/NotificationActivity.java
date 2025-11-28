package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvTitle;
    private LinearLayout homeNav, cartNav, profileNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        // Ánh xạ các view
        notificationContainer = findViewById(R.id.notificationContainer);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Thông báo");

        homeNav = findViewById(R.id.home);
        cartNav = findViewById(R.id.cart);
        profileNav = findViewById(R.id.profile);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Tải danh sách thông báo
        loadNotifications();

        // Thiết lập sự kiện cho thanh điều hướng
        setupBottomNavigation();
    }

    private void loadNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationContainer.removeAllViews();
                        if (task.getResult().isEmpty()) {
                            displayEmptyMessage();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                NotificationModel notification = document.toObject(NotificationModel.class);
                                String notificationId = document.getId();
                                addNotificationItemView(notification, notificationId);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Lỗi khi tải thông báo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addNotificationItemView(NotificationModel notification, final String notificationId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.notification_item, notificationContainer, false);

        TextView titleView = itemView.findViewById(R.id.notification_title);
        TextView contentView = itemView.findViewById(R.id.notification_content);
        Button cancelButton = itemView.findViewById(R.id.cancel_button);

        titleView.setText(notification.getTitle());

        String originalContent = notification.getContent();
        if (originalContent != null) {
            String[] parts = originalContent.split("\n");
            String totalPart = null;
            StringBuilder otherParts = new StringBuilder();

            for (String part : parts) {
                if (part.toLowerCase().contains("tổng")) {
                    totalPart = part;
                } else {
                    if (otherParts.length() > 0) {
                        otherParts.append("\n");
                    }
                    otherParts.append(part);
                }
            }

            String newContent;
            if (totalPart != null) {
                if (otherParts.length() > 0) {
                    newContent = otherParts.toString() + "\n" + totalPart;
                } else {
                    newContent = totalPart;
                }
            } else {
                newContent = originalContent;
            }
            contentView.setText(newContent);
        } else {
            contentView.setText("");
        }

        cancelButton.setOnClickListener(v -> {
            notificationContainer.removeView(itemView);
            deleteNotificationFromFirestore(notificationId);
        });

        notificationContainer.addView(itemView);
    }

    private void deleteNotificationFromFirestore(String notificationId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || notificationId == null) return;
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                    if (notificationContainer.getChildCount() == 0) {
                        displayEmptyMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa thông báo", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
    }

    private void displayEmptyMessage() {
        TextView emptyView = new TextView(this);
        emptyView.setText("Bạn chưa có thông báo nào.");
        emptyView.setTextSize(16);
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setPadding(0, 100, 0, 0);
        notificationContainer.addView(emptyView);
    }


    private void setupBottomNavigation() {
        // Chuyển về Home
        homeNav.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Chuyển sang Giỏ hàng
        cartNav.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Chuyển sang trang Tôi
        profileNav.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
