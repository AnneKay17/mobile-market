package com.example.mobilemarketapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {

    // ─── Database metadata ────────────────────────────────────────────────────
    private static final String DATABASE_NAME    = "MarketApp.db";
    private static final int    DATABASE_VERSION = 4; // bumped to trigger onUpgrade

    // ─── Table: Items ─────────────────────────────────────────────────────────
    private static final String TABLE_ITEMS      = "Items";
    private static final String COL_ITEM_ID      = "itemId";
    private static final String COL_NAME         = "name";
    private static final String COL_DESCRIPTION  = "description";
    private static final String COL_PRICE        = "price";
    private static final String COL_SELLER       = "sellerName";
    private static final String COL_DATE         = "datePosted";
    private static final String COL_CATEGORY     = "category";
    private static final String COL_IMAGES       = "imageUris"; // comma-separated URIs

    // ─── Table: Ratings ───────────────────────────────────────────────────────
    private static final String TABLE_RATINGS    = "Ratings";
    private static final String COL_RATING_ID    = "ratingId";
    private static final String COL_RATING_ITEM  = "itemId";   // FK → Items.itemId
    private static final String COL_RATING_USER  = "userName";
    private static final String COL_RATING_VAL   = "rating";

    // ─── Table: Users ─────────────────────────────────────────────────────────
    private static final String TABLE_USERS      = "Users";
    private static final String COL_USER_ID      = "userId";
    private static final String COL_USERNAME     = "username";
    private static final String COL_EMAIL        = "email";
    private static final String COL_PASSWORD     = "password"; // stored as SHA-256 hash

    // ─── Constructor ──────────────────────────────────────────────────────────
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // enforce referential integrity
    }

    // ─── Schema creation ──────────────────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Items table — stores every listed product
        String createItems =
                "CREATE TABLE " + TABLE_ITEMS + " (" +
                        COL_ITEM_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME        + " TEXT NOT NULL, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_PRICE       + " REAL NOT NULL, " +
                        COL_SELLER      + " TEXT NOT NULL, " +
                        COL_DATE        + " INTEGER NOT NULL, " +   // epoch ms from System.currentTimeMillis()
                        COL_CATEGORY    + " TEXT, " +
                        COL_IMAGES      + " TEXT" +                 // comma-joined URI strings
                        ")";

        // Ratings table — one row per (user, item) pair, enforced by UNIQUE constraint
        // ON DELETE CASCADE automatically removes ratings when the parent item is deleted
        String createRatings =
                "CREATE TABLE " + TABLE_RATINGS + " (" +
                        COL_RATING_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RATING_ITEM + " INTEGER NOT NULL, " +
                        COL_RATING_USER + " TEXT NOT NULL, " +
                        COL_RATING_VAL  + " INTEGER NOT NULL, " +
                        "UNIQUE(" + COL_RATING_ITEM + ", " + COL_RATING_USER + "), " +
                        "FOREIGN KEY(" + COL_RATING_ITEM + ") REFERENCES " +
                        TABLE_ITEMS + "(" + COL_ITEM_ID + ") ON DELETE CASCADE" +
                        ")";

        // Users table — unique username and email enforced at DB level
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        COL_EMAIL    + " TEXT UNIQUE NOT NULL, " +
                        COL_PASSWORD + " TEXT NOT NULL" +           // SHA-256 hash, never plain text
                        ")";

        db.execSQL(createItems);
        db.execSQL(createRatings);
        db.execSQL(createUsers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS); // drop child first (FK)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));

            // Convert byte array to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return password; // fallback (should never happen on Android)
        }
    }

    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL,    email);
        values.put(COL_PASSWORD, hashPassword(password)); // store hash, not plain text
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1; // -1 means insert failed (e.g. UNIQUE constraint violated)
    }


    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashed = hashPassword(password); // hash before comparing
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL    + " = ? AND " +
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
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?",
                new String[]{email}
        );
    }

    public boolean insertItem(String name, String description, double price,
                              String sellerName, String category, String imageUris) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME,        name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_PRICE,       price);
        values.put(COL_SELLER,      sellerName);
        values.put(COL_DATE,        System.currentTimeMillis()); // timestamp in ms
        values.put(COL_CATEGORY,    category);
        values.put(COL_IMAGES,      imageUris);
        long result = db.insert(TABLE_ITEMS, null, values);
        return result != -1;
    }

    public ArrayList<Item> getAllItemsList() {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COL_DATE + " DESC",
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


    private Item cursorToItem(Cursor cursor) {
        // Parse the comma-separated image URI string back into a list
        String imagesStr = cursor.getString(7);
        ArrayList<String> images = new ArrayList<>();
        if (imagesStr != null && !imagesStr.isEmpty()) {
            for (String uri : imagesStr.split(",")) {
                images.add(uri.trim());
            }
        }
        return new Item(
                cursor.getInt(0),    // itemId
                cursor.getString(1), // name
                cursor.getString(2), // description
                cursor.getDouble(3), // price
                cursor.getString(4), // sellerName
                cursor.getLong(5),   // datePosted
                cursor.getString(6), // category
                images               // imageUris list
        );
    }


    public boolean hasUserRated(int itemId, String userName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RATINGS +
                        " WHERE " + COL_RATING_ITEM + " = ? AND " + COL_RATING_USER + " = ?",
                new String[]{String.valueOf(itemId), userName}
        );
        boolean rated = cursor.getCount() > 0;
        cursor.close();
        return rated;
    }


    public boolean addRating(int itemId, String userName, int rating) {
        // Check the one-rating-per-user rule before inserting
        if (hasUserRated(itemId, userName)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RATING_ITEM, itemId);
        values.put(COL_RATING_USER, userName);
        values.put(COL_RATING_VAL,  rating);
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
            average = cursor.getDouble(0); // AVG returns NULL for no rows → 0.0
        }
        cursor.close();
        return average;
    }
}
