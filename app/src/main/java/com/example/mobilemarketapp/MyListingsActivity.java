package com.example.mobilemarketapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyListingsActivity extends AppCompatActivity {

    RecyclerView myListingsRecycler;
    TextView emptyListingsText;

    MyListingsAdapter adapter;
    DBHelper dbHelper;

    ArrayList<Item> myItems;
    String sellerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        dbHelper = new DBHelper(this);

        sellerName = getSharedPreferences("app", MODE_PRIVATE)
                .getString("user", "Unknown");

        myListingsRecycler = findViewById(R.id.myListingsRecycler);
        emptyListingsText = findViewById(R.id.emptyListingsText);

        myListingsRecycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadMyListings();
    }

    private void loadMyListings() {

        myItems = dbHelper.getItemsBySeller(sellerName);

        adapter = new MyListingsAdapter(
                myItems,
                dbHelper,
                sellerName,
                this::loadMyListings
        );

        myListingsRecycler.setAdapter(adapter);

        boolean empty = myItems.isEmpty();

        emptyListingsText.setVisibility(
                empty ? View.VISIBLE : View.GONE
        );

        myListingsRecycler.setVisibility(
                empty ? View.GONE : View.VISIBLE
        );
    }
}