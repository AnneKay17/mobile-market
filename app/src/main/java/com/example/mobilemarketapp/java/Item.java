package com.example.mobilemarketapp.java;

import java.util.List;

public class Item {
    public int itemId;
    public String name;
    public String description;
    public double price;
    public String sellerName;
    public long datePosted;
    public String category;
    public List<String> imageUris;

    // Full constructor - used when loading from database
    public Item(int itemId, String name, String description,
                double price, String sellerName, long datePosted,
                String category, List<String> imageUris) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;
        this.datePosted = datePosted;
        this.category = category;
        this.imageUris = imageUris;
    }

    // Short constructor - used in cart
    public Item(String name, String description, double price,
                String sellerName, String category,
                List<String> imageUris) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;
        this.datePosted = System.currentTimeMillis();
        this.category = category;
        this.imageUris = imageUris;
    }
    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getSellerName() { return sellerName; }
    public long getDatePosted() { return datePosted; }
    public String getCategory() { return category; }
    public List<String> getImageUris() { return imageUris; }
}
