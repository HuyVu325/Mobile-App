package com.example.tuan_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private GridView gridView;
    private LinearLayout CartButton, profile;
    private SearchView searchView;

    // Dữ liệu gốc từ Firestore
    private ArrayList<String> allProductIds = new ArrayList<>();
    private ArrayList<String> allNames = new ArrayList<>();
    private ArrayList<String> allPrices = new ArrayList<>();
    private ArrayList<String> allImageUrls = new ArrayList<>();
    private ArrayList<String> allDescriptions = new ArrayList<>();

    // Dữ liệu đang hiển thị (sau khi lọc)
    private ArrayList<String> productIds = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> prices = new ArrayList<>();
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<String> descriptions = new ArrayList<>();

    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        db = FirebaseFirestore.getInstance();
        gridView = findViewById(R.id.gridView);
        searchView = findViewById(R.id.searchView);
        CartButton = findViewById(R.id.cart);
        profile = findViewById(R.id.profile);

        // Chuyen qua trang Toi
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Tải sản phẩm từ Firestore
        loadProductsFromFirestore();

        // Mở giỏ hàng
        CartButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Khi bấm vào 1 sản phẩm
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("product_id", productIds.get(position));
            intent.putExtra("product_name", names.get(position));
            intent.putExtra("product_price", prices.get(position));
            intent.putExtra("product_image", imageUrls.get(position));
            intent.putExtra("product_desc", descriptions.get(position));
            startActivity(intent);
        });

        // Search
        setupSearch();
    }

    private void loadProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnSuccessListener(q -> {
                    allProductIds.clear();
                    allNames.clear();
                    allPrices.clear();
                    allImageUrls.clear();
                    allDescriptions.clear();

                    for (QueryDocumentSnapshot doc : q) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String imageUrl = doc.getString("imageUrl");
                        String desc = doc.getString("description");

                        if (name == null) name = "";
                        if (imageUrl == null) imageUrl = "";
                        if (desc == null) desc = "";
                        String priceText = (price != null ? price : 0) + "đ";

                        allProductIds.add(id);
                        allNames.add(name);
                        allPrices.add(priceText);
                        allImageUrls.add(imageUrl);
                        allDescriptions.add(desc);
                    }
                    // ban đầu hiển thị tất cả
                    applyFilter("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupSearch() {
        // cho search luôn mở
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilter(newText);
                return true;
            }
        });
    }

    private void applyFilter(String query) {
        String q = (query == null ? "" : query.toLowerCase().trim());

        productIds.clear();
        names.clear();
        prices.clear();
        imageUrls.clear();
        descriptions.clear();

        for (int i = 0; i < allNames.size(); i++) {
            String name = allNames.get(i).toLowerCase();
            String desc = allDescriptions.get(i).toLowerCase();

            // lọc theo tên hoặc mô tả, bạn muốn chỉ tên thì bỏ "|| desc.contains(q)"
            if (q.isEmpty() || name.contains(q) || desc.contains(q)) {
                productIds.add(allProductIds.get(i));
                names.add(allNames.get(i));
                prices.add(allPrices.get(i));
                imageUrls.add(allImageUrls.get(i));
                descriptions.add(allDescriptions.get(i));
            }
        }

        // tạo adapter mới mỗi lần lọc (đơn giản, không cần updateData)
        adapter = new GridAdapter(this, names, imageUrls, prices);
        gridView.setAdapter(adapter);
    }
}
