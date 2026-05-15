package com.example.mobilemarketapp;

import java.util.List;

//--ITEM MODEL: THIS REPRESENTS ONE PRODUCT(CONTAINER FOR DATA)
public class Item {
    //Properties of one item in the app
    String name; //stores item name
    String description; // stores item description
    double price; //store item price
    String sellerName; // stores seller name
    String datePosted; // date item was posted
    String category; // category item falls under

    List<String> imageUris; // stores item images
    //Constructor: runs when a new Item object is CREATED
    public Item(String name,String description, double price, String sellerName, String datePosted, String category, List<String> imageUris){
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;
        this.datePosted = datePosted;
        this.category = category;
        this.imageUris = imageUris;

    }
}
