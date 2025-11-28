package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvUsername, tvEmail;
    private Button btnLogout, btnAdmin, btnHistory, btnFavorites;

    private LinearLayout navHome, navCart, navProfile, notification;
    private LinearLayout layoutUserInfoCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnHistory = findViewById(R.id.btnHistory);
        btnFavorites = findViewById(R.id.btnFavorites);

        navHome = findViewById(R.id.home);
        navCart = findViewById(R.id.cart);
        notification = findViewById(R.id.announcement);
        navProfile = findViewById(R.id.profile);
        layoutUserInfoCard = findViewById(R.id.layoutUserInfoCard);


        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();
        tvEmail.setText(email != null ? email : "Không có email");

        // Load username + role
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String role = doc.getString("role");

                        if (username == null || username.isEmpty()) {
                            username = "Người dùng";
                        }
                        tvUsername.setText(username);

                        if (role != null && role.equalsIgnoreCase("admin")) {
                            btnAdmin.setVisibility(android.view.View.VISIBLE);
                        } else {
                            btnAdmin.setVisibility(android.view.View.GONE);
                        }
                    } else {
                        tvUsername.setText("Người dùng");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Bấm vào card -> mở màn sửa thông tin
        layoutUserInfoCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Danh sách yêu thích
        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
        });

        // Lịch sử mua hàng
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Vào trang Admin (chỉ cho admin)
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Thanh điều hướng dưới
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
            startActivity(intent);
        });

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
            startActivity(intent);
        });
        navProfile.setOnClickListener(v -> {
            // đang ở tab Tôi, không làm gì
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi sửa info xong quay lại, có thể reload lại tên/email nếu muốn
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            tvEmail.setText(email != null ? email : "Không có email");
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.getString("username");
                        if (username == null || username.isEmpty()) username = "Người dùng";
                        tvUsername.setText(username);
                    });
        }
    }
}
