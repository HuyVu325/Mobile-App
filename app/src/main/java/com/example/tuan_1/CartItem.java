package com.example.tuan_1;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String name;
    private String imageUrl;
    private long price;
    private int buyQuantity;
    private int quantity;
    private String docId;

    public CartItem(String name, String imageUrl, long price, int buyQuantity, int quantity, String docId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.buyQuantity = buyQuantity;
        this.quantity = quantity;
        this.docId = docId;
    }

    // Getters
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public long getPrice() { return price; }
    public int getBuyQuantity() { return buyQuantity; }
    public int getQuantity() { return quantity; }
    public String getDocId() { return docId; }


    protected CartItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        price = in.readLong();
        buyQuantity = in.readInt();
        quantity = in.readInt();
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
        dest.writeInt(quantity);
        dest.writeString(docId);
    }
}
