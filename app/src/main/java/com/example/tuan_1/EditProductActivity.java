package com.example.tuan_1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 200;

    private EditText edtName, edtPrice, edtDescription;
    private ImageView imgPreview, btnBackAdd;
    private Button btnChooseImage, btnSaveProduct;
    private Spinner spinnerCategory;

    private Uri newImageUri = null;      // ảnh mới (nếu có chọn lại)
    private String currentImageUrl = ""; // ảnh cũ
    private String selectedCategory = "fruit";
    private String productId;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        // Nhận id sản phẩm
        productId = getIntent().getStringExtra("product_id");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Thiếu product_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnBackAdd = findViewById(R.id.btnBackAdd);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        btnSaveProduct.setText("Cập nhật sản phẩm");

        btnBackAdd.setOnClickListener(v -> {
            startActivity(new Intent(EditProductActivity.this, ProductManagementActivity.class));
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("product_images");

        // Spinner category: Fruit / Vegetable
        String[] display = {"Fruit", "Vegetable"};
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                display
        );
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) selectedCategory = "fruit";
                else selectedCategory = "vegetable";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Load thông tin sản phẩm để hiển thị
        loadProduct();

        // Chọn ảnh mới
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), PICK_IMAGE_REQUEST);
        });

        // Lưu thay đổi
        btnSaveProduct.setOnClickListener(v -> saveChanges());
    }

    private void loadProduct() {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(this::fillProductData)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fillProductData(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = doc.getString("name");
        Double price = doc.getDouble("price");
        String desc = doc.getString("description");
        currentImageUrl = doc.getString("imageUrl");
        String category = doc.getString("category");

        if (name != null) edtName.setText(name);
        if (price != null) edtPrice.setText(String.valueOf(Math.round(price)));
        if (desc != null) edtDescription.setText(desc);

        if (category != null) {
            if (category.equalsIgnoreCase("vegetable")) {
                spinnerCategory.setSelection(1);
                selectedCategory = "vegetable";
            } else {
                spinnerCategory.setSelection(0);
                selectedCategory = "fruit";
            }
        }

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.shop)
                    .error(R.drawable.shop)
                    .into(imgPreview);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            newImageUri = data.getData();
            imgPreview.setImageURI(newImageUri);
        }
    }

    private void saveChanges() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu không đổi ảnh -> update thẳng
        if (newImageUri == null) {
            updateProductInFirestore(name, price, desc, currentImageUrl);
        } else {
            // Có chọn ảnh mới -> upload ảnh rồi update
            uploadNewImageAndUpdate(name, price, desc);
        }
    }

    private void uploadNewImageAndUpdate(String name, double price, String desc) {
        Toast.makeText(this, "Đang upload ảnh mới...", Toast.LENGTH_SHORT).show();

        String fileName = "edit_" + System.currentTimeMillis() + ".png";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String newImageUrl = uri.toString();
                            updateProductInFirestore(name, price, desc, newImageUrl);
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload ảnh lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateProductInFirestore(String name, double price, String desc, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("description", desc);
        updates.put("imageUrl", imageUrl);
        updates.put("category", selectedCategory);

        db.collection("products").document(productId)
                .update(updates)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
