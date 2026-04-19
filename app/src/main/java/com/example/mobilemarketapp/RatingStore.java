package com.example.mobilemarketapp;

import java.util.ArrayList;
import java.util.List;

//TEMP STORAGE LAYER, REPLACED WITH SQL LATER
// This enforces the rule: each user can only rate an item once
public class RatingStore {

    //temporary in-memory storage of all ratings (acts like database)
    public static List<Rating> ratings = new ArrayList<>();

    //checks if user already rated item
    public static boolean hasRated(int itemId, String userName){
        //loops through temp storage
        for(Rating r: ratings){
            //checks if both itemId and userName already exists inside ratings
            if(r.itemId == itemId && r.userName.equals(userName)){
                return true; //user already rated this item
            }
        }
        return false; // no match,user has not rated yet
    }

    //Adds a new rating into the temp staorage lis
    public static void addRating(Rating rating){
        ratings.add(rating);
    }

    //Calculates the average rating for the specific item
    public static double getAverage(int itemId){
        int sum = 0; //sum of all ratings for this item
        int count = 0; // number of ratings for this item

        //loops through temp storage
        for(Rating r : ratings){

            //consider only ratings for the eslected item
            if(r.itemId == itemId){
                sum += r.rating; //adds to rating score to sum
                count ++; // increases number of ratings
            }
        }
        //avoid diving by zero
        if(count == 0){
            return 0;
        }
        return (double) sum/ count; // returns average rating
    }


}
