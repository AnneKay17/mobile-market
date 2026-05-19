package com.example.mobilemarketapp;

public class Rating {
    public int ratingId;
    public int itemId;
    public String userName;
    public int rating;

    public Rating(int ratingId, int itemId,
                  String userName, int rating) {
        this.ratingId = ratingId;
        this.itemId = itemId;
        this.userName = userName;
        this.rating = rating;
    }

    public int getRatingId() { return ratingId; }
    public int getItemId() { return itemId; }
    public String getUserName() { return userName; }
    public int getRating() { return rating; }
}
