package com.example.mobilemarketapp;

import java.util.ArrayList;
import java.util.List;

public class ItemStore {

    //SHARED LIST FOR WHOLE APP
    public static List <Item> items = new ArrayList<>();
    static {

        // fake starter items
        items.add(new Item(
                "Shoes",
                "Running shoes",
                500,
                "Admin",
                "2026",
                "Fashion",
                new ArrayList<>()
        ));

        items.add(new Item(
                "Phone",
                "Samsung phone",
                3000,
                "Admin",
                "2026",
                "Electronics",
                new ArrayList<>()
        ));
    }

}
