package com.example.tuan_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
    private final Context context;
    private final String[] items;
    private final int[] images;
    private final String[] prices;

    public GridAdapter(Context context, String[] items, int[] images, String[] prices) {
        this.context = context;
        this.items = items;
        this.images = images;
        this.prices = prices;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getPrice(int position) { return prices[position]; }

    public String[] getPrices() { return prices; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.gridImage);
        TextView textView = convertView.findViewById(R.id.gridText);
        TextView priceView = convertView.findViewById(R.id.gridPrice);

        imageView.setImageResource(images[position]);
        textView.setText(items[position]);
        priceView.setText(prices[position]);

        return convertView;
    }
}