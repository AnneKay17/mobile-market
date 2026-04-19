package com.example.mobilemarketapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ItemAdapter adapter;
    List<Item> itemList;
    SearchView search_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connects screen to activity_main.xml
        setContentView(R.layout.activity_main);

        // finds RecyclerView from xml
        recyclerView = findViewById(R.id.recyclerView);

        // Find search bar from xml
        search_bar = findViewById(R.id.searchBar);

        // tell RecyclerView to display items vertically
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create empty list to store items
        itemList = new ArrayList<>();

        //Temp data(remove later and replace with sql data)
        itemList.add(new Item("Shoes", "Nice running shoes", 500, "Karabo"));
        itemList.add(new Item("Phone", "Samsung S24",2300, "Oratile"));

        // connect data to adapter
        adapter = new ItemAdapter(itemList);

        // connects adapter to RecyclerView
        recyclerView.setAdapter(adapter);

        //Add listener to detect typing in search bar
        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query); // when user presses search
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // live filtering while typing
                return false;
            }
        });
    }
}

//Brains of UI: Loads screen, create data, connects adapter and shows list