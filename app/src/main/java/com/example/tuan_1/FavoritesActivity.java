package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {

    private ImageView btnBackFavorite;
    private GridView gridFavorites;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Dữ liệu yêu thích
    private ArrayList<String> productIds = new ArrayList<>();
    private ArrayList<String> names      = new ArrayList<>();
    private ArrayList<String> prices     = new ArrayList<>();
    private ArrayList<String> imageUrls  = new ArrayList<>();
    private ArrayList<String> descriptions = new ArrayList<>();
    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);

        btnBackFavorite = findViewById(R.id.btnBackFavorite);
        gridFavorites   = findViewById(R.id.gridFavorites);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        btnBackFavorite.setOnClickListener(v -> onBackPressed());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
            finish();
            return;
        }

        loadFavorites();

        // Bấm vào 1 sản phẩm yêu thích -> mở DetailActivity
        gridFavorites.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            intent.putExtra("product_id", productIds.get(position));
            intent.putExtra("product_name", names.get(position));
            intent.putExtra("product_price", prices.get(position));
            intent.putExtra("product_image", imageUrls.get(position));
            intent.putExtra("product_desc", descriptions.get(position));
            startActivity(intent);
        });

        // Nhấn giữ để xóa khỏi yêu thích
        gridFavorites.setOnItemLongClickListener((parent, view, position, id) -> {
            removeFromFavorites(position);
            return true;
        });
    }

    private void loadFavorites() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(q -> {
                    productIds.clear();
                    names.clear();
                    prices.clear();
                    imageUrls.clear();
                    descriptions.clear();

                    for (QueryDocumentSnapshot doc : q) {
                        String productId = doc.getId();
                        String name = doc.getString("name");
                        String price = doc.getString("price");
                        String imageUrl = doc.getString("imageUrl");

                        if (name == null) name = "";
                        if (price == null) price = "";
                        if (imageUrl == null) imageUrl = "";

                        productIds.add(productId);
                        names.add(name);
                        prices.add(price);
                        imageUrls.add(imageUrl);
                        descriptions.add("");
                    }

                    adapter = new GridAdapter(FavoritesActivity.this, names, imageUrls, prices);
                    gridFavorites.setAdapter(adapter);

                    if (names.isEmpty()) {
                        Toast.makeText(this, "Chưa có sản phẩm yêu thích nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeFromFavorites(int position) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String productId = productIds.get(position);

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(productId)
                .delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();

                    // xóa khỏi list hiện tại + cập nhật adapter
                    productIds.remove(position);
                    names.remove(position);
                    prices.remove(position);
                    imageUrls.remove(position);
                    descriptions.remove(position);

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
