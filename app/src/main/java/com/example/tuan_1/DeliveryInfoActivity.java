package com.example.tuan_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class DeliveryInfoActivity extends AppCompatActivity {

    private EditText edtName, edtVoucher, edtNote;
    private TextView tvTotalPrice;
    private Button btnBack, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_info);

        // Lấy tổng tiền từ Intent
        long total = getIntent().getLongExtra("totalPrice", 0);

        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtVoucher = findViewById(R.id.edtVoucher);
        edtNote = findViewById(R.id.edtNote);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Hiển thị tổng tiền
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng tiền: " + nf.format(total) + "đ");

        // Quay lại trang trước
        btnBack.setOnClickListener(v -> finish());

        // Xác nhận mua hàng
        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String voucher = edtVoucher.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên người nhận!", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Lưu thông tin đơn hàng vào Firestore nếu cần
            Toast.makeText(this, "Đặt hàng thành công! Shipper sẽ liên hệ với bạn.", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
