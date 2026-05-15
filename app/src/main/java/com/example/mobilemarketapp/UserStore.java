package com.example.mobilemarketapp;

import java.util.ArrayList;
import java.util.List;

//TEMP STORAGE LAYER
public class UserStore {

    //Temp storage of users
    public static List<User> users = new ArrayList<>();

    //Adds in user to test login
    static {
        users.add(new User("admin", "admin@gmail.com", "1234"));
    }
    //Register a new user
    public static boolean register(String userName, String userEmail, String password){

        //Check if the user already exists
        for(User u : users){
            if(u.userEmail.equals(userEmail)){
                return  false; // Email taken
            }
        }
        users.add(new User(userName, userEmail, password));
        return true; //user added
    }

    //Login validation
    public static boolean login(String userEmail, String password){

        //Check if user exists and password is correct
        for(User u : users){
            if(u.userEmail.equals(userEmail) && u.password.equals(password)){
                return true; //login successful
            }
            //improvement: send warning for incorrect password


        }
        return false;// login failed
    }
}
