package com.example.tuan_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

    // đổi pass
    private EditText edtCurrentPassword, edtNewPassword, edtConfirmNewPassword;
    private Button btnChangePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Ánh xạ view
        btnBackEditProfile   = findViewById(R.id.btnBackEditProfile);
        tvEmailEdit          = findViewById(R.id.tvEmailEdit);
        edtUsernameEdit      = findViewById(R.id.edtUsernameEdit);
        edtPhoneEdit         = findViewById(R.id.edtPhoneEdit);
        edtAddressEdit       = findViewById(R.id.edtAddressEdit);
        btnSaveEditProfile   = findViewById(R.id.btnSaveEditProfile);

        edtCurrentPassword   = findViewById(R.id.edtCurrentPassword);
        edtNewPassword       = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword= findViewById(R.id.edtConfirmNewPassword);
        btnChangePassword    = findViewById(R.id.btnChangePassword);

        btnBackEditProfile.setOnClickListener(v -> onBackPressed());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid   = user.getUid();
        String email = user.getEmail();
        tvEmailEdit.setText(email != null ? email : "Không có email");

        // Load thông tin từ Firestore
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String phone    = doc.getString("phone");
                        String address  = doc.getString("address");

                        if (username != null) edtUsernameEdit.setText(username);
                        if (phone != null)    edtPhoneEdit.setText(phone);
                        if (address != null)  edtAddressEdit.setText(address);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Lưu thông tin (username / phone / address)
        btnSaveEditProfile.setOnClickListener(v -> saveProfile());

        // Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    // Lưu thông tin vào Firestore
    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid       = user.getUid();
        String username  = edtUsernameEdit.getText().toString().trim();
        String phone     = edtPhoneEdit.getText().toString().trim();
        String address   = edtAddressEdit.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("username", username);
        update.put("phone", phone);
        update.put("address", address);

        db.collection("users").document(uid)
                .update(update)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Đổi mật khẩu trên FirebaseAuth
    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String currentPass = edtCurrentPassword.getText().toString().trim();
        String newPass     = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmNewPassword.getText().toString().trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 3 ô mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu mới nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "Không tìm thấy email tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate với mật khẩu hiện tại
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPass);

        user.reauthenticate(credential)
                .addOnSuccessListener(a -> {
                    // Re-authen OK -> update password
                    user.updatePassword(newPass)
                            .addOnSuccessListener(b -> {
                                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                // clear ô nhập
                                edtCurrentPassword.setText("");
                                edtNewPassword.setText("");
                                edtConfirmNewPassword.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show());
    }
}
