package com.example.tuan_1;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class QRPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_payment);

        ImageView qrImage = findViewById(R.id.qrImage);

//        // Ảnh QR của bạn (đặt trong drawable)
//        qrImage.setImageResource(R.drawable.qr_code_example);
    }
}
