package com.example.mobilemarketapp.java;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mobilemarketapp.Item;

import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * DBHelper — Central database manager for Mobile Market.
 *
 * Tables:
 *   Users   — registered accounts (username, email, hashed password)
 *   Items   — listed products (name, description, price, seller, date, category, images)
 *   Ratings — one row per user-per-item rating (enforces the "rate once" rule)
 *
 * Design decisions:
 *   - Passwords are SHA-256 hashed before storage (never stored plain text)
 *   - SQLite foreign keys are enabled via onConfigure() so cascade deletes work
 *   - Ratings.itemId references Items.itemId ON DELETE CASCADE
 */
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

    /**
     * Called before onCreate / onUpgrade — enables SQLite foreign key support.
     * Without this PRAGMA, ON DELETE CASCADE has no effect.
     */
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

    /** Drop all tables and recreate when the version number increases. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS); // drop child first (FK)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PASSWORD HASHING
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Hashes a plain-text password using SHA-256.
     * Passwords are NEVER stored in plain text.
     *
     * @param password The raw password entered by the user.
     * @return Hex-encoded SHA-256 hash, or the original string if hashing fails.
     */
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

    // ═════════════════════════════════════════════════════════════════════════
    //  USER METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Registers a new user account.
     * The password is hashed with SHA-256 before being stored.
     *
     * @param username Chosen display name (must be unique).
     * @param email    User's email address (must be unique).
     * @param password Plain-text password — hashed inside this method.
     * @return true if inserted successfully, false if username/email already exists.
     */
    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL,    email);
        values.put(COL_PASSWORD, hashPassword(password)); // store hash, not plain text
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1; // -1 means insert failed (e.g. UNIQUE constraint violated)
    }

    /**
     * Validates login credentials.
     * The supplied password is hashed and compared against the stored hash.
     *
     * @param email    Email entered on the login screen.
     * @param password Plain-text password entered on the login screen.
     * @return true if a matching user record is found.
     */
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

    /**
     * Returns a Cursor for the user row with the given email.
     * Column order: userId(0), username(1), email(2), password(3)
     */
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?",
            new String[]{email}
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ITEM METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Inserts a new item into the database.
     * datePosted is set automatically to the current time (epoch ms).
     *
     * @param name        Item name.
     * @param description Detailed description.
     * @param price       Asking price in Rands.
     * @param sellerName  Username of the logged-in seller.
     * @param category    Category chosen from the spinner.
     * @param imageUris   Comma-separated content URIs (may be empty string).
     * @return true if the insert succeeded.
     */
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

    /**
     * Returns all items as an ArrayList, ordered newest first.
     * Used by MainActivity to populate the RecyclerView.
     */
    public ArrayList<com.example.mobilemarketapp.Item> getAllItemsList() {
        ArrayList<com.example.mobilemarketapp.Item> list = new ArrayList<>();
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

    /**
     * Searches items whose name contains the query string (case-insensitive LIKE).
     * Results are still ordered newest first.
     *
     * @param query The text typed in the search bar.
     * @return Filtered list of matching items.
     */
    public ArrayList<com.example.mobilemarketapp.Item> searchItemsList(String query) {
        ArrayList<com.example.mobilemarketapp.Item> list = new ArrayList<>();
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

    /**
     * Converts a Cursor row into an Item object.
     * Extracted to avoid repeating the same column index logic in multiple methods.
     *
     * Column order from schema:
     *   0 itemId | 1 name | 2 description | 3 price | 4 sellerName
     *   5 datePosted | 6 category | 7 imageUris
     */
    private com.example.mobilemarketapp.Item cursorToItem(Cursor cursor) {
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

    // ═════════════════════════════════════════════════════════════════════════
    //  RATING METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Checks whether a user has already submitted a rating for a specific item.
     * Used to enforce the "one rating per user per item" business rule.
     *
     * @param itemId   The item being rated.
     * @param userName The logged-in user's username.
     * @return true if the user has already rated this item.
     */
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

    /**
     * Inserts a rating for an item.
     * Returns false (and does not insert) if the user has already rated this item.
     *
     * The UNIQUE(itemId, userName) constraint in the schema provides a second
     * layer of protection even if hasUserRated() is bypassed somehow.
     *
     * @param itemId   The item being rated.
     * @param userName The logged-in user's username.
     * @param rating   Integer star value 1–5.
     * @return true if the rating was inserted, false if already rated.
     */
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

    /**
     * Calculates the average rating for an item.
     *
     * @param itemId The item whose ratings are being averaged.
     * @return Average as a double, or 0.0 if no ratings exist yet.
     */
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
