package com.example.tuan_1;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // ĐĂNG NHẬP
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(MainActivity.this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(MainActivity.this, "Lỗi: không lấy được thông tin user", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // sau khi login xong cũng kiểm tra BAN rồi mới cho vào home
                            checkBannedAndGo(user);

                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Đăng nhập thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // CHUYỂN SANG TRANG ĐĂNG KÝ
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // 👉 HÀM GIỮ TRẠNG THÁI ĐĂNG NHẬP
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // user đã đăng nhập trước đó -> kiểm tra BAN rồi vào Home
            checkBannedAndGo(currentUser);
        }
        // nếu currentUser == null thì để user ở lại màn login bình thường
    }

    // Hàm dùng chung: kiểm tra bị BAN hay không
    private void checkBannedAndGo(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        // nếu chưa có document users -> cho vào app bình thường
                        goToHome();
                        return;
                    }

                    Boolean banned = doc.getBoolean("isBanned");
                    if (banned != null && banned) {
                        Toast.makeText(MainActivity.this,
                                "Tài khoản của bạn đã bị khóa bởi Admin!",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    } else {
                        goToHome();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "Lỗi kiểm tra trạng thái tài khoản: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // không quay lại login nữa
    }
}
