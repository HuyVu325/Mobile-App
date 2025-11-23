package com.example.tuan_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    private Context context;
    private List<String> names;
    private List<String> prices;
    private List<String> imageUrls;
    private LayoutInflater inflater;

    public GridAdapter(Context context,
                       List<String> names,
                       List<String> imageUrls,
                       List<String> prices) {
        this.context = context;
        this.names = names;
        this.prices = prices;
        this.imageUrls = imageUrls;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView txtName, txtPrice;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            h = new ViewHolder();
            h.imageView = convertView.findViewById(R.id.gridImage);
            h.txtName = convertView.findViewById(R.id.gridText);
            h.txtPrice = convertView.findViewById(R.id.gridPrice);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        h.txtName.setText(names.get(position));
        h.txtPrice.setText(prices.get(position));

        Glide.with(context)
                .load(imageUrls.get(position))
                .placeholder(R.drawable.shop)
                .error(R.drawable.shop)
                .into(h.imageView);

        return convertView;
    }
}
