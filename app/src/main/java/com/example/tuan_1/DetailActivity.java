package com.example.tuan_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgProduct, btnBack, btnMinus, btnPlus;
    private TextView tvName, tvPrice, tvQuantity, tvRatingCount, tvDescription;
    private Button btnAddToCart, btnSubmitReview;
    private RatingBar ratingUser, ratingAverage;
    private EditText edtComment;
    private LinearLayout layoutComments;

    private int quantity = 1;
    private String name, price, imageUrl, desc, productId;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        imgProduct = findViewById(R.id.imgProduct);
        btnBack = findViewById(R.id.btnBack);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvDescription = findViewById(R.id.tvDescription); // 👈 thêm
        btnAddToCart = findViewById(R.id.btnAddToCart);

        ratingUser = findViewById(R.id.ratingUser);
        ratingAverage = findViewById(R.id.ratingAverage);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        layoutComments = findViewById(R.id.layoutComments);

        // Nhận dữ liệu từ Intent
        name = getIntent().getStringExtra("product_name");
        price = getIntent().getStringExtra("product_price");
        imageUrl = getIntent().getStringExtra("product_image");
        desc = getIntent().getStringExtra("product_desc"); // nếu có
        productId = getIntent().getStringExtra("product_id");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Thiếu product_id, không thể tải đánh giá", Toast.LENGTH_SHORT).show();
            // vẫn cho xem chi tiết cơ bản
        }

        tvName.setText(name != null ? name : "");
        tvPrice.setText(price != null ? price : "");

        // Hiển thị mô tả
        if (desc != null && !desc.trim().isEmpty()) {
            tvDescription.setText(desc);
        } else {
            tvDescription.setText("Chưa có mô tả cho sản phẩm này.");
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.shop)
                .error(R.drawable.shop)
                .into(imgProduct);

        tvQuantity.setText(String.valueOf(quantity));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .whereEqualTo("productId", productId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        if (!querySnapshot.isEmpty()) {
                            String docId = querySnapshot.getDocuments().get(0).getId();
                            Long oldQuantity = querySnapshot.getDocuments().get(0).getLong("quantity");
                            if (oldQuantity == null) oldQuantity = 0L;
                            long newQuantity = oldQuantity + quantity;

                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .update("quantity", newQuantity)
                                    .addOnSuccessListener(a ->
                                            Toast.makeText(this, "Đã cập nhật số lượng trong giỏ hàng!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Map<String, Object> cartItem = new HashMap<>();
                            cartItem.put("productId", productId);
                            cartItem.put("name", name);
                            cartItem.put("imageUrl", imageUrl);
                            cartItem.put("price", price);
                            cartItem.put("quantity", quantity);
                            cartItem.put("createdAt", FieldValue.serverTimestamp());

                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .add(cartItem)
                                    .addOnSuccessListener(doc ->
                                            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Lỗi thêm mới: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi truy vấn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Gửi đánh giá
        btnSubmitReview.setOnClickListener(v -> submitReview());

        // Load danh sách review
        if (productId != null && !productId.isEmpty()) {
            loadReviews();
        }
    }

    private void submitReview() {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không xác định được sản phẩm để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingUser.getRating();
        String commentText = edtComment.getText().toString().trim();

        if (rating <= 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(docUser -> {
                    String username = docUser.getString("username");
                    if (username == null || username.isEmpty()) {
                        username = user.getEmail() != null ? user.getEmail() : "Người dùng";
                    }

                    Map<String, Object> review = new HashMap<>();
                    review.put("userId", uid);
                    review.put("userName", username);
                    review.put("comment", commentText);
                    review.put("rating", rating);
                    review.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("products")
                            .document(productId)
                            .collection("reviews")
                            .add(review)
                            .addOnSuccessListener(rv -> {
                                edtComment.setText("");
                                ratingUser.setRating(0f);
                                Toast.makeText(this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                                loadReviews();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lấy thông tin user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadReviews() {
        db.collection("products")
                .document(productId)
                .collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    layoutComments.removeAllViews();

                    int count = 0;
                    float totalRating = 0f;

                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (QueryDocumentSnapshot doc : query) {
                        String userName = doc.getString("userName");
                        String comment = doc.getString("comment");
                        Double ratingVal = doc.getDouble("rating");

                        if (userName == null) userName = "Người dùng";
                        if (comment == null) comment = "";
                        float r = ratingVal != null ? ratingVal.floatValue() : 0f;

                        totalRating += r;
                        count++;

                        android.view.View itemView = inflater.inflate(R.layout.item_comment, layoutComments, false);
                        TextView tvUserName = itemView.findViewById(R.id.tvUserName);
                        TextView tvComment = itemView.findViewById(R.id.tvComment);
                        RatingBar ratingItem = itemView.findViewById(R.id.ratingUserItem);

                        tvUserName.setText(userName);
                        tvComment.setText(comment);
                        ratingItem.setRating(r);

                        layoutComments.addView(itemView);
                    }

                    if (count > 0) {
                        float avg = totalRating / count;
                        ratingAverage.setRating(avg);
                        tvRatingCount.setText("(" + count + " đánh giá)");
                    } else {
                        ratingAverage.setRating(0f);
                        tvRatingCount.setText("(chưa có đánh giá)");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
