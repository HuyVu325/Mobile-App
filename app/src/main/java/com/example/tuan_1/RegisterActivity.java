package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword1, edtPassword2;
    private Button btnRegister, btnBack;

    private FirebaseAuth mAuth;  // Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword1 = findViewById(R.id.edtPassword1);
        edtPassword2 = findViewById(R.id.edtPassword2);
        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);

        // quay lại trang login
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // đăng ký tài khoản mới
        btnRegister.setOnClickListener(v -> {
            String email = edtUsername.getText().toString().trim();
            String pass1 = edtPassword1.getText().toString().trim();
            String pass2 = edtPassword2.getText().toString().trim();

            if (email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
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

            // Gọi Firebase để tạo tài khoản
            mAuth.createUserWithEmailAndPassword(email, pass1)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                            // quay về màn đăng nhập
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Đăng ký thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
