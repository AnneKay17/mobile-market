package com.example.mobilemarketapp;

//---- Mock Database structure for testing(Future database structure)
public class Rating {
    int itemId;
    String userName;
    int rating;

    public Rating(int itemId, String userName, int rating){
        this.itemId = itemId;
        this.userName = userName;
        this.rating = rating;
    }
}
