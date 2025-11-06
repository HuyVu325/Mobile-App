package com.example.tuan_1;

import android.os.Bundle;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tuan_1.GridAdapter;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        GridView gridView = findViewById(R.id.gridView);

        // Danh sách chữ
        String[] items = {"Apple", "Banana", "Cherry", "Date", "Grapes"};

        // Danh sách hình (đặt file ảnh trong res/drawable/)
        int[] images = {
                R.drawable.apple,   // apple.png trong drawable
                R.drawable.banana,  // banana.png
                R.drawable.cherry,  // cherry.png
                R.drawable.date,    // date.png
                R.drawable.grapes   // grapes.png
        };

        // Tạo adapter và gắn vào GridView
        GridAdapter adapter = new GridAdapter(this, items, images);
        gridView.setAdapter(adapter);
    }
}