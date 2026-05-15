package com.example.mobilemarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ItemAdapter adapter;
    List<Item> itemList;
    SearchView search_bar;
    //FloatingActionButton postItemBtn;
    BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connects screen to activity_main.xml
        setContentView(R.layout.activity_main);

        // finds RecyclerView from xml
        recyclerView = findViewById(R.id.recyclerView);

        // Find search bar from xml
        search_bar = findViewById(R.id.searchBar);
        //postItemBtn = findViewById(R.id.postItemBtn);

        //bottom navbar
        bottomNav = findViewById(R.id.bottomNav);

        // tell RecyclerView to display items vertically
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Items get updated and recycler view sees the data
        itemList = ItemStore.items;

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
        //What?
        /*postItemBtn.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    PostItemActivity.class
            );

            startActivity(intent);
        });*/

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // already on home
                return true;
            }

            else if (id == R.id.nav_post) {
                startActivity(new Intent(this, PostItemActivity.class));
                return true;
            }

            else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class)); //needs a database
                return true;
            }

            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });




    }
    //Refresh RecyclerView after posting
    @Override
    protected void onResume() {
        super.onResume();

        adapter.refreshList();
    }
}

//Brains of UI: Loads screen, create data, connects adapter and shows list