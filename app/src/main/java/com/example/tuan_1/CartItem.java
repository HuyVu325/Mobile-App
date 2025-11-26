package com.example.tuan_1;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String name;
    private String imageUrl;
    private long price;
    private int buyQuantity; // Số lượng mua
    private String docId; // ID của document trong Firestore

    public CartItem(String name, String imageUrl, long price, int buyQuantity, String docId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.buyQuantity = buyQuantity;
        this.docId = docId;
    }

    // Getters
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public long getPrice() { return price; }
    public int getBuyQuantity() { return buyQuantity; }
    public String getDocId() { return docId; }


    // --- Parcelable Implementation ---
    protected CartItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        price = in.readLong();
        buyQuantity = in.readInt();
        docId = in.readString();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeLong(price);
        dest.writeInt(buyQuantity);
        dest.writeString(docId);
    }
}
