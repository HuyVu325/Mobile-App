package com.example.tuan_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBackEditProfile;
    private TextView tvEmailEdit;
    private EditText edtUsernameEdit, edtPhoneEdit, edtAddressEdit;
    private Button btnSaveEditProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBackEditProfile = findViewById(R.id.btnBackEditProfile);
        tvEmailEdit = findViewById(R.id.tvEmailEdit);
        edtUsernameEdit = findViewById(R.id.edtUsernameEdit);
        edtPhoneEdit = findViewById(R.id.edtPhoneEdit);
        edtAddressEdit = findViewById(R.id.edtAddressEdit);
        btnSaveEditProfile = findViewById(R.id.btnSaveEditProfile);

        btnBackEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = user.getUid();
        String email = user.getEmail();
        tvEmailEdit.setText(email != null ? email : "Không có email");

        // Load info hiện tại
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");

                        if (username != null) edtUsernameEdit.setText(username);
                        if (phone != null) edtPhoneEdit.setText(phone);
                        if (address != null) edtAddressEdit.setText(address);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        btnSaveEditProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String username = edtUsernameEdit.getText().toString().trim();
        String phone = edtPhoneEdit.getText().toString().trim();
        String address = edtAddressEdit.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phone", phone);
        updates.put("address", address);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
