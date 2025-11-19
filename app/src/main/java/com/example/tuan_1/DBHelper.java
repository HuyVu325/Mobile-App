package com.example.tuan_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fruit_shop.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USER = "users";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_FULLNAME = "fullname";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USER + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT, "
                + COL_FULLNAME + " TEXT, "
                + COL_EMAIL + " TEXT, "
                + COL_PHONE + " TEXT)";
        db.execSQL(createUserTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // đơn giản: xóa và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // Thêm user mới (dùng lúc Đăng ký)
    public boolean insertUser(String username, String password,
                              String fullname, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_FULLNAME, fullname);
        values.put(COL_EMAIL, email);
        values.put(COL_PHONE, phone);

        long result = db.insert(TABLE_USER, null, values);
        return result != -1; // true nếu insert thành công
    }

    // Kiểm tra đăng nhập
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_USER +
                " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});
        boolean ok = cursor.getCount() > 0;
        cursor.close();
        return ok;
    }

    // Lấy thông tin user theo username
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_USER +
                " WHERE " + COL_USERNAME + "=?";
        return db.rawQuery(sql, new String[]{username});
    }
}
