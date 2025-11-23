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
    private LinearLayout CartButton;
    private SearchView searchView;

    private ArrayList<String> allNames = new ArrayList<>();
    private ArrayList<String> allPrices = new ArrayList<>();
    private ArrayList<String> allImageUrls = new ArrayList<>();
    private ArrayList<String> allDescriptions = new ArrayList<>();

    // danh sách đang hiển thị (sau filter)
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

        loadProductsFromFirestore();

        CartButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("product_name", names.get(position));
            intent.putExtra("product_price", prices.get(position));
            intent.putExtra("product_image", imageUrls.get(position));
            intent.putExtra("product_desc", descriptions.get(position));
            startActivity(intent);
        });

        setupSearch();
    }

    private void loadProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnSuccessListener(q -> {
                    allNames.clear();
                    allPrices.clear();
                    allImageUrls.clear();
                    allDescriptions.clear();

                    for (QueryDocumentSnapshot doc : q) {
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String imageUrl = doc.getString("imageUrl");
                        String desc = doc.getString("description");

                        if (name == null) name = "";
                        if (imageUrl == null) imageUrl = "";
                        if (desc == null) desc = "";
                        String priceText = (price != null ? price : 0) + "đ";

                        allNames.add(name);
                        allPrices.add(priceText);
                        allImageUrls.add(imageUrl);
                        allDescriptions.add(desc);
                    }

                    applyFilter("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupSearch() {
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
        String q = query == null ? "" : query.toLowerCase().trim();

        names.clear();
        prices.clear();
        imageUrls.clear();
        descriptions.clear();

        for (int i = 0; i < allNames.size(); i++) {
            String name = allNames.get(i);
            if (q.isEmpty() || name.toLowerCase().contains(q)) {
                names.add(allNames.get(i));
                prices.add(allPrices.get(i));
                imageUrls.add(allImageUrls.get(i));
                descriptions.add(allDescriptions.get(i));
            }
        }

        adapter = new GridAdapter(this, names, imageUrls, prices);
        gridView.setAdapter(adapter);
    }
}
