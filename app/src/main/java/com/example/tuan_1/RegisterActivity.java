package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword1, edtPassword2, edtUsername;
    private Button btnRegister, btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword1 = findViewById(R.id.edtPassword1);
        edtPassword2 = findViewById(R.id.edtPassword2);
        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);

        // Nút quay lại Login
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        // Nút đăng ký
        btnRegister.setOnClickListener(v -> {

            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass1 = edtPassword1.getText().toString().trim();
            String pass2 = edtPassword2.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass1.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Auth tạo tài khoản
            mAuth.createUserWithEmailAndPassword(email, pass1)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // Dữ liệu lưu vào Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("username", username);
                            userData.put("email", email);
                            userData.put("createdAt", System.currentTimeMillis());

                            db.collection("users")
                                    .document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this,
                                                "Đăng ký thành công",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
                                            "Lưu thông tin lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Đăng ký thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
