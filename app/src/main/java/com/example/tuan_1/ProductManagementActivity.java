package com.example.tuan_1;

import android.content.DialogInterface;
import android.content.Intent;
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

public class ProductManagementActivity extends AppCompatActivity {

    private ListView listProducts;
    private ImageView btnBackProduct;

    private FirebaseFirestore db;

    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private ArrayList<String> productIds = new ArrayList<>();
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_management);

        listProducts = findViewById(R.id.listProducts);
        btnBackProduct = findViewById(R.id.btnBackProduct);
        db = FirebaseFirestore.getInstance();

        btnBackProduct.setOnClickListener(v -> {
            finish();
        });

        adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_product_admin,
                new String[]{"name", "price", "category"},
                new int[]{R.id.tvProductNameAdmin, R.id.tvProductPriceAdmin, R.id.tvProductCategoryAdmin}
        );
        listProducts.setAdapter(adapter);

        loadProducts();

        // Chạm -> sửa (mở EditProductActivity)
        listProducts.setOnItemClickListener((parent, view, position, id) -> {
            String productId = productIds.get(position);
            Intent intent = new Intent(ProductManagementActivity.this, EditProductActivity.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        });

        // Nhấn giữ -> hỏi xóa
        listProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            String productId = productIds.get(position);
            String productName = data.get(position).get("name");
            showDeleteDialog(productId, productName);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi sửa xong quay lại -> load lại danh sách
        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(q -> {
                    data.clear();
                    productIds.clear();

                    for (QueryDocumentSnapshot doc : q) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String category = doc.getString("category");

                        if (name == null) name = "(no name)";
                        if (category == null) category = "fruit";

                        long priceLong = (price != null) ? Math.round(price) : 0;
                        String priceText = "Giá: " + priceLong + "đ";
                        String categoryText = "Loại: " + category;

                        productIds.add(id);

                        Map<String, String> map = new HashMap<>();
                        map.put("name", name);
                        map.put("price", priceText);
                        map.put("category", categoryText);
                        data.add(map);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog(String productId, String productName) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm:\n\"" + productName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("products")
                            .document(productId)
                            .delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
