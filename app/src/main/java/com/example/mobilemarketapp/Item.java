package com.example.mobilemarketapp;
//--ITEM MODEL: THIS REPRESENTS ONE PRODUCT(CONTAINER FOR DATA)
public class Item {
    //Properties of one item in the app
    String name; //stores item name
    String description; // stores item description
    double price; //store item price
    String sellerName; // stores seller name


    //Constructor: runs when a new Item object is CREATED
    public Item(String name,String description, double price, String sellerName){
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;

    }
}
