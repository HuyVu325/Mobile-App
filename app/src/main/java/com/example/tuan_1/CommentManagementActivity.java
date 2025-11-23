package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentManagementActivity extends AppCompatActivity {

    private ListView listComments;
    private FirebaseFirestore db;

    private ImageView btnBackComment;
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private ArrayList<String> reviewDocPaths = new ArrayList<>(); // lưu path để xóa
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_management);

        listComments = findViewById(R.id.listComments);
        btnBackComment = findViewById(R.id.btnBackComment);
        db = FirebaseFirestore.getInstance();

        btnBackComment.setOnClickListener(v ->
        {
            Intent intent = new Intent(CommentManagementActivity.this, AdminActivity.class);
            startActivity(intent);
        });


        adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_comment_admin,
                new String[]{"productId", "userName", "comment", "ratingText"},
                new int[]{R.id.tvProductId, R.id.tvUserNameComment, R.id.tvCommentText, R.id.tvRatingValue}
        );
        listComments.setAdapter(adapter);

        loadComments();

        listComments.setOnItemLongClickListener((parent, view, position, id) -> {
            String path = reviewDocPaths.get(position);
            String cmt = data.get(position).get("comment");
            showDeleteCommentDialog(path, cmt, position);
            return true;
        });
    }

    private void loadComments() {
        db.collectionGroup("reviews")
                .get()
                .addOnSuccessListener(q -> {
                    data.clear();
                    reviewDocPaths.clear();
                    for (QueryDocumentSnapshot doc : q) {
                        // path dạng: products/{productId}/reviews/{reviewId}
                        String fullPath = doc.getReference().getPath();
                        reviewDocPaths.add(fullPath);

                        // tách productId
                        String[] parts = fullPath.split("/");
                        String productId = parts.length >= 2 ? parts[1] : "(unknown)";

                        String userName = doc.getString("userName");
                        String comment = doc.getString("comment");
                        Double rating = doc.getDouble("rating");

                        if (userName == null) userName = "Người dùng";
                        if (comment == null) comment = "";
                        double r = rating != null ? rating : 0.0;

                        Map<String, String> map = new HashMap<>();
                        map.put("productId", "Sản phẩm: " + productId);
                        map.put("userName", userName);
                        map.put("comment", comment);
                        map.put("ratingText", "★ " + r);
                        data.add(map);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteCommentDialog(String docPath, String comment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc muốn xóa bình luận:\n\"" + comment + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.document(docPath)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                data.remove(position);
                                reviewDocPaths.remove(position);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
