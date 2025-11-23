package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity {

    private ListView listUsers;
    private FirebaseFirestore db;

    private ImageView btnBackUser;
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

        btnBackUser.setOnClickListener(v -> {
            Intent intent = new Intent(UserManagementActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_user_admin,
                new String[]{"username", "email"},
                new int[]{R.id.tvUserName, R.id.tvUserEmail}
        );
        listUsers.setAdapter(adapter);

        loadUsers();

        // nhấn lâu để xóa user trong collection users (chỉ ví dụ)
        listUsers.setOnItemLongClickListener((parent, view, position, id) -> {
            String userId = userIds.get(position);
            String username = data.get(position).get("username");
            showDeleteUserDialog(userId, username, position);
            return true;
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
                        if (username == null) username = "(no name)";
                        if (email == null) email = "(no email)";
                        userIds.add(id);

                        Map<String, String> map = new HashMap<>();
                        map.put("username", username);
                        map.put("email", email);
                        data.add(map);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteUserDialog(String userId, String username, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa user")
                .setMessage("Bạn có chắc muốn xóa user \"" + username + "\" khỏi collection users?\n(Lưu ý: không xóa tài khoản trong FirebaseAuth)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("users")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                data.remove(position);
                                userIds.remove(position);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Đã xóa user", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
