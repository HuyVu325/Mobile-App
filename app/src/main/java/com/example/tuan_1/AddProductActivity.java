package com.example.tuan_1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private EditText edtName, edtPrice, edtDescription;
    private ImageView imgPreview, btnBackAdd;
    private Button btnChooseImage, btnSaveProduct;

    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnBackAdd = findViewById(R.id.btnBackAdd);

        btnBackAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("product_images");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), PICK_IMAGE_REQUEST);
        });

        btnSaveProduct.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImageAndSaveProduct(name, price, desc);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSaveProduct(String name, double price, String desc) {
        Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();

        String fileName = System.currentTimeMillis() + ".png";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            String uid = mAuth.getCurrentUser() != null
                                    ? mAuth.getCurrentUser().getUid()
                                    : "unknown";

                            Map<String, Object> product = new HashMap<>();
                            product.put("name", name);
                            product.put("price", price);
                            product.put("description", desc);
                            product.put("imageUrl", imageUrl);
                            product.put("ownerId", uid);
                            product.put("createdAt", FieldValue.serverTimestamp());

                            db.collection("products")
                                    .add(product)
                                    .addOnSuccessListener(doc -> {
                                        Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Lỗi lưu sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload ảnh lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
