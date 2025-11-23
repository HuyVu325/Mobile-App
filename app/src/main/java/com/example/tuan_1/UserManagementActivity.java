package com.example.tuan_1;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity {

    private ListView listUsers;
    private ImageView btnBackUser;
    private FirebaseFirestore db;

    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private ArrayList<String> userIds = new ArrayList<>();
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_management);

        listUsers = findViewById(R.id.listUsers);
        btnBackUser = findViewById(R.id.btnBackUser);
        db = FirebaseFirestore.getInstance();

        btnBackUser.setOnClickListener(v -> onBackPressed());

        adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_user_admin,          // layout 1 dòng user
                new String[]{"username", "email"}, // key trong map
                new int[]{R.id.tvUserName, R.id.tvUserEmail} // id TextView
        );
        listUsers.setAdapter(adapter);

        loadUsers();

        // Nhấn vào 1 user -> hiện dialog BAN user đó
        listUsers.setOnItemClickListener((parent, view, position, id) -> {
            String userId = userIds.get(position);
            String username = data.get(position).get("username");
            showBanUserDialog(userId, username);
        });
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(q -> {
                    data.clear();
                    userIds.clear();
                    for (QueryDocumentSnapshot doc : q) {
                        String id = doc.getId();
                        String username = doc.getString("username");
                        String email = doc.getString("email");
                        Boolean isBanned = doc.getBoolean("isBanned");

                        if (username == null) username = "(no name)";
                        if (email == null) email = "(no email)";

                        String displayName = username;
                        if (isBanned != null && isBanned) {
                            displayName += " (ĐÃ KHÓA)";
                        }

                        userIds.add(id);

                        Map<String, String> map = new HashMap<>();
                        map.put("username", displayName);
                        map.put("email", email);
                        data.add(map);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showBanUserDialog(String userId, String username) {
        new AlertDialog.Builder(this)
                .setTitle("Khóa tài khoản")
                .setMessage("Bạn có chắc muốn KHÓA user:\n\"" + username + "\"?\n\n" +
                        "User bị khóa sẽ không thể đăng nhập vào app nữa.")
                .setPositiveButton("Khóa", (dialog, which) -> {
                    db.collection("users")
                            .document(userId)
                            .update("isBanned", true)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Đã khóa user", Toast.LENGTH_SHORT).show();
                                loadUsers(); // load lại để hiện chữ (ĐÃ KHÓA)
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
