package com.example.mobilemarketapp;

import java.util.ArrayList;
import java.util.List;

public class ItemStore {

    // SHARED LIST FOR WHOLE APP
    public static List<Item> items = new ArrayList<>();

    static {

        // Fake starter items
        items.add(new Item(
                "Shoes",
                "Running shoes",
                500.0,
                "Admin",
                "Fashion",
                new ArrayList<String>()
        ));

        items.add(new Item(
                "Gaming Laptop",
                "Good condition laptop",
                12000.0,
                "Alex",
                "Electronics",
                new ArrayList<String>()
        ));

        items.add(new Item(
                "Phone",
                "Samsung phone",
                3000.0,
                "Admin",
                "Electronics",
                new ArrayList<String>()
        ));
    }
}