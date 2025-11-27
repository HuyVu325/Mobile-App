package com.example.tuan_1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VoucherActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout voucherContainer;
    private ImageView btnBackVoucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        db = FirebaseFirestore.getInstance();
        voucherContainer = findViewById(R.id.voucherContainer);
        btnBackVoucher = findViewById(R.id.btnBackVoucher);

        FloatingActionButton btnAddVoucher = findViewById(R.id.btnAddVoucher);
        btnAddVoucher.setOnClickListener(v -> showAddVoucherDialog());

        btnBackVoucher.setOnClickListener(v -> finish());

        loadVouchers();
    }

    private void loadVouchers() {
        db.collection("vouchers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        voucherContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addVoucherView(document);
                        }
                    } else {
                        Toast.makeText(VoucherActivity.this, "Lỗi khi tải voucher.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addVoucherView(QueryDocumentSnapshot document) {
        View voucherItemView = LayoutInflater.from(this).inflate(R.layout.item_voucher, voucherContainer, false);

        TextView tvVoucherName = voucherItemView.findViewById(R.id.tvVoucherName);
        TextView tvDiscount = voucherItemView.findViewById(R.id.tvDiscount);
        TextView tvStartDate = voucherItemView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = voucherItemView.findViewById(R.id.tvEndDate);
        Button btnEdit = voucherItemView.findViewById(R.id.btnEditVoucher);
        Button btnDelete = voucherItemView.findViewById(R.id.btnDeleteVoucher);

        tvVoucherName.setText("Mã: " + document.getString("name"));
        tvDiscount.setText("Giảm giá: " + document.getLong("discount") + "%");
        tvStartDate.setText("Ngày bắt đầu: " + document.getString("startDate"));
        tvEndDate.setText("Ngày kết thúc: " + document.getString("endDate"));

        btnEdit.setOnClickListener(v -> showEditVoucherDialog(document));
        btnDelete.setOnClickListener(v -> deleteVoucher(document.getId()));

        voucherContainer.addView(voucherItemView);
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            calendar.set(year1, monthOfYear, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            editText.setText(sdf.format(calendar.getTime()));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showAddVoucherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Voucher");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_voucher, null);
        builder.setView(view);

        final EditText etName = view.findViewById(R.id.etVoucherName);
        final EditText etDiscount = view.findViewById(R.id.etVoucherDiscount);
        final EditText etStartDate = view.findViewById(R.id.etVoucherStartDate);
        final EditText etEndDate = view.findViewById(R.id.etVoucherEndDate);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String discountStr = etDiscount.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(discountStr) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long discount = Long.parseLong(discountStr);
                if (discount > 100) {
                    Toast.makeText(this, "Phần trăm giảm giá không được vượt quá 100%", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> voucher = new HashMap<>();
                voucher.put("name", name);
                voucher.put("discount", discount);
                voucher.put("startDate", startDate);
                voucher.put("endDate", endDate);

                db.collection("vouchers").add(voucher)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Thêm voucher thành công", Toast.LENGTH_SHORT).show();
                            loadVouchers();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Thêm voucher thất bại", Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phần trăm giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditVoucherDialog(QueryDocumentSnapshot document) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa Voucher");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_voucher, null);
        builder.setView(view);

        final EditText etName = view.findViewById(R.id.etVoucherName);
        final EditText etDiscount = view.findViewById(R.id.etVoucherDiscount);
        final EditText etStartDate = view.findViewById(R.id.etVoucherStartDate);
        final EditText etEndDate = view.findViewById(R.id.etVoucherEndDate);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        etName.setText(document.getString("name"));
        etDiscount.setText(String.valueOf(document.getLong("discount")));
        etStartDate.setText(document.getString("startDate"));
        etEndDate.setText(document.getString("endDate"));

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String discountStr = etDiscount.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(discountStr) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long discount = Long.parseLong(discountStr);
                if (discount > 100) {
                    Toast.makeText(this, "Phần trăm giảm giá không được vượt quá 100%", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> voucher = new HashMap<>();
                voucher.put("name", name);
                voucher.put("discount", discount);
                voucher.put("startDate", startDate);
                voucher.put("endDate", endDate);

                db.collection("vouchers").document(document.getId())
                        .set(voucher)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Cập nhật voucher thành công", Toast.LENGTH_SHORT).show();
                            loadVouchers();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật voucher thất bại", Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phần trăm giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteVoucher(String documentId) {
        db.collection("vouchers").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa voucher thành công", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa voucher thất bại", Toast.LENGTH_SHORT).show());
    }
}
