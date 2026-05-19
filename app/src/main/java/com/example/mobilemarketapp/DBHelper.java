package com.example.mobilemarketapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MarketApp.db";
    private static final int DATABASE_VERSION = 6;

    private static final String TABLE_ITEMS = "Items";
    private static final String COL_ITEM_ID = "itemId";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PRICE = "price";
    private static final String COL_SELLER = "sellerName";
    private static final String COL_DATE = "datePosted";
    private static final String COL_CATEGORY = "category";
    private static final String COL_IMAGES = "imageUris";
    private static final String COL_STATUS = "status";

    private static final String TABLE_RATINGS = "Ratings";
    private static final String COL_RATING_ID = "ratingId";
    private static final String COL_RATING_ITEM = "itemId";
    private static final String COL_RATING_USER = "userName";
    private static final String COL_RATING_VAL = "rating";

    private static final String TABLE_USERS = "Users";
    private static final String COL_USER_ID = "userId";
    private static final String COL_USERNAME = "username";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    private static final String TABLE_CART = "Cart";
    private static final String COL_CART_ID = "cartId";
    private static final String COL_CART_USER_EMAIL = "userEmail";
    private static final String COL_CART_ITEM_ID = "itemId";

    private static final String TABLE_COMMENTS = "Comments";
    private static final String COL_COMMENT_ID = "commentId";
    private static final String COL_COMMENT_ITEM_ID = "itemId";
    private static final String COL_COMMENT_USER = "userName";
    private static final String COL_COMMENT_TEXT = "commentText";
    private static final String COL_COMMENT_DATE = "datePosted";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createItems =
                "CREATE TABLE " + TABLE_ITEMS + " (" +
                        COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_PRICE + " REAL NOT NULL, " +
                        COL_SELLER + " TEXT NOT NULL, " +
                        COL_DATE + " INTEGER NOT NULL, " +
                        COL_CATEGORY + " TEXT, " +
                        COL_IMAGES + " TEXT, " +
                        COL_STATUS + " TEXT DEFAULT 'available'" +
                        ")";

        String createRatings =
                "CREATE TABLE " + TABLE_RATINGS + " (" +
                        COL_RATING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RATING_ITEM + " INTEGER NOT NULL, " +
                        COL_RATING_USER + " TEXT NOT NULL, " +
                        COL_RATING_VAL + " INTEGER NOT NULL, " +
                        "UNIQUE(" + COL_RATING_ITEM + ", " + COL_RATING_USER + "), " +
                        "FOREIGN KEY(" + COL_RATING_ITEM + ") REFERENCES " +
                        TABLE_ITEMS + "(" + COL_ITEM_ID + ") ON DELETE CASCADE" +
                        ")";

        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                        COL_PASSWORD + " TEXT NOT NULL" +
                        ")";

        String createCart =
                "CREATE TABLE " + TABLE_CART + " (" +
                        COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CART_USER_EMAIL + " TEXT NOT NULL, " +
                        COL_CART_ITEM_ID + " INTEGER NOT NULL, " +
                        "UNIQUE(" + COL_CART_USER_EMAIL + ", " + COL_CART_ITEM_ID + "), " +
                        "FOREIGN KEY(" + COL_CART_ITEM_ID + ") REFERENCES " +
                        TABLE_ITEMS + "(" + COL_ITEM_ID + ") ON DELETE CASCADE" +
                        ")";

        String createComments =
                "CREATE TABLE " + TABLE_COMMENTS + " (" +
                        COL_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_COMMENT_ITEM_ID + " INTEGER NOT NULL, " +
                        COL_COMMENT_USER + " TEXT NOT NULL, " +
                        COL_COMMENT_TEXT + " TEXT NOT NULL, " +
                        COL_COMMENT_DATE + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + COL_COMMENT_ITEM_ID + ") REFERENCES " +
                        TABLE_ITEMS + "(" + COL_ITEM_ID + ") ON DELETE CASCADE" +
                        ")";

        db.execSQL(createItems);
        db.execSQL(createRatings);
        db.execSQL(createUsers);
        db.execSQL(createCart);
        db.execSQL(createComments);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashed = hashPassword(password);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL + " = ? AND " +
                        COL_PASSWORD + " = ?",
                new String[]{email, hashed}
        );

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL + " = ?",
                new String[]{email}
        );
    }

    public boolean resetPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, hashPassword(newPassword));

        int result = db.update(
                TABLE_USERS,
                values,
                COL_EMAIL + "=?",
                new String[]{email}
        );

        return result > 0;
    }

    public boolean insertItem(String name, String description, double price,
                              String sellerName, String category, String imageUris) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_PRICE, price);
        values.put(COL_SELLER, sellerName);
        values.put(COL_DATE, System.currentTimeMillis());
        values.put(COL_CATEGORY, category);
        values.put(COL_IMAGES, imageUris);
        values.put(COL_STATUS, "available");

        long result = db.insert(TABLE_ITEMS, null, values);
        return result != -1;
    }

    public ArrayList<Item> getAllItemsList() {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS +
                        " ORDER BY " + COL_DATE + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public ArrayList<Item> searchItemsList(String query) {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS +
                        " WHERE " + COL_NAME + " LIKE ?" +
                        " ORDER BY " + COL_DATE + " DESC",
                new String[]{"%" + query + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public ArrayList<Item> getItemsBySeller(String sellerName) {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS +
                        " WHERE " + COL_SELLER + " = ?" +
                        " ORDER BY " + COL_DATE + " DESC",
                new String[]{sellerName}
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    private Item cursorToItem(Cursor cursor) {
        String imagesStr = cursor.getString(7);

        ArrayList<String> images = new ArrayList<>();

        if (imagesStr != null && !imagesStr.isEmpty()) {
            for (String uri : imagesStr.split(",")) {
                images.add(uri.trim());
            }
        }

        return new Item(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getDouble(3),
                cursor.getString(4),
                cursor.getLong(5),
                cursor.getString(6),
                images,
                cursor.getString(8)
        );
    }

    public boolean hasUserRated(int itemId, String userName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RATINGS +
                        " WHERE " + COL_RATING_ITEM + " = ? AND " +
                        COL_RATING_USER + " = ?",
                new String[]{String.valueOf(itemId), userName}
        );

        boolean rated = cursor.getCount() > 0;
        cursor.close();
        return rated;
    }

    public boolean addRating(int itemId, String userName, int rating) {
        if (hasUserRated(itemId, userName)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_RATING_ITEM, itemId);
        values.put(COL_RATING_USER, userName);
        values.put(COL_RATING_VAL, rating);

        long result = db.insert(TABLE_RATINGS, null, values);
        return result != -1;
    }

    public double getAverageRating(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT AVG(" + COL_RATING_VAL + ") FROM " + TABLE_RATINGS +
                        " WHERE " + COL_RATING_ITEM + " = ?",
                new String[]{String.valueOf(itemId)}
        );

        double average = 0.0;

        if (cursor.moveToFirst()) {
            average = cursor.getDouble(0);
        }

        cursor.close();
        return average;
    }

    public boolean addToCart(String userEmail, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (isItemSold(itemId)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_CART_USER_EMAIL, userEmail);
        values.put(COL_CART_ITEM_ID, itemId);

        long result = db.insert(TABLE_CART, null, values);
        return result != -1;
    }

    public boolean isItemInCart(String userEmail, int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_CART +
                        " WHERE " + COL_CART_USER_EMAIL + " = ? AND " +
                        COL_CART_ITEM_ID + " = ?",
                new String[]{userEmail, String.valueOf(itemId)}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<Item> getCartItems(String userEmail) {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT I.* FROM " + TABLE_ITEMS + " I " +
                        "INNER JOIN " + TABLE_CART + " C ON I." + COL_ITEM_ID +
                        " = C." + COL_CART_ITEM_ID +
                        " WHERE C." + COL_CART_USER_EMAIL + " = ?",
                new String[]{userEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public boolean removeFromCart(String userEmail, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_CART,
                COL_CART_USER_EMAIL + "=? AND " + COL_CART_ITEM_ID + "=?",
                new String[]{userEmail, String.valueOf(itemId)}
        );

        return result > 0;
    }

    public boolean checkoutCart(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Item> cartItems = getCartItems(userEmail);

        if (cartItems.isEmpty()) {
            return false;
        }

        for (Item item : cartItems) {
            ContentValues values = new ContentValues();
            values.put(COL_STATUS, "sold");

            db.update(
                    TABLE_ITEMS,
                    values,
                    COL_ITEM_ID + "=?",
                    new String[]{String.valueOf(item.getItemId())}
            );
        }

        db.delete(
                TABLE_CART,
                COL_CART_USER_EMAIL + "=?",
                new String[]{userEmail}
        );

        return true;
    }

    public boolean isItemSold(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_STATUS + " FROM " + TABLE_ITEMS +
                        " WHERE " + COL_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)}
        );

        boolean sold = false;

        if (cursor.moveToFirst()) {
            String status = cursor.getString(0);
            sold = status != null && status.equalsIgnoreCase("sold");
        }

        cursor.close();
        return sold;
    }

    public boolean markItemAsSold(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "sold");

        int result = db.update(
                TABLE_ITEMS,
                values,
                COL_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        return result > 0;
    }

    public boolean markSellerItemAsSold(int itemId, String sellerName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "sold");

        int result = db.update(
                TABLE_ITEMS,
                values,
                COL_ITEM_ID + "=? AND " + COL_SELLER + "=?",
                new String[]{String.valueOf(itemId), sellerName}
        );

        return result > 0;
    }

    public boolean deleteItem(int itemId, String sellerName) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_ITEMS,
                COL_ITEM_ID + "=? AND " + COL_SELLER + "=?",
                new String[]{String.valueOf(itemId), sellerName}
        );

        return result > 0;
    }

    public int countItemsBySeller(String sellerName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ITEMS +
                        " WHERE " + COL_SELLER + " = ?",
                new String[]{sellerName}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    public int countItemsBySellerAndStatus(String sellerName, String status) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ITEMS +
                        " WHERE " + COL_SELLER + " = ? AND " +
                        COL_STATUS + " = ?",
                new String[]{sellerName, status}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    public boolean addComment(int itemId, String userName, String commentText) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_COMMENT_ITEM_ID, itemId);
        values.put(COL_COMMENT_USER, userName);
        values.put(COL_COMMENT_TEXT, commentText);
        values.put(COL_COMMENT_DATE, System.currentTimeMillis());

        long result = db.insert(TABLE_COMMENTS, null, values);
        return result != -1;
    }

    public ArrayList<String> getCommentsForItem(int itemId) {
        ArrayList<String> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_COMMENT_USER + ", " +
                        COL_COMMENT_TEXT +
                        " FROM " + TABLE_COMMENTS +
                        " WHERE " + COL_COMMENT_ITEM_ID + " = ?" +
                        " ORDER BY " + COL_COMMENT_DATE + " DESC",
                new String[]{String.valueOf(itemId)}
        );

        if (cursor.moveToFirst()) {
            do {
                String user = cursor.getString(0);
                String text = cursor.getString(1);

                comments.add(user + ": " + text);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return comments;
    }
    public void seedItemsIfEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ITEMS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        if (count == 0) {
            insertItem("Gaming Laptop", "Good condition laptop", 12000.0, "Admin", "Electronics", "");
            insertItem("Running Shoes", "Nike running shoes", 500.0, "Admin", "Clothing", "");
            insertItem("Samsung Phone", "Samsung Galaxy S21", 3000.0, "Admin", "Electronics", "");
        }
    }
}
