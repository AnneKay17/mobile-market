package com.example.mobilemarketapp.java;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilemarketapp.CartActivity;
import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.Item;
import com.example.mobilemarketapp.ItemAdapter;
import com.example.mobilemarketapp.PostItemActivity;
import com.example.mobilemarketapp.ProfileActivity;
import com.example.mobilemarketapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * MainActivity — The main marketplace screen.
 *
 * Shows all posted items in a RecyclerView (newest first).
 * Includes a live search bar that filters items by name.
 * Bottom navigation links to: Home | Post Item | Basket | Profile
 *
 * onResume() refreshes the list so newly posted items appear immediately
 * when the user navigates back from PostItemActivity.
 */
public class MainActivity extends AppCompatActivity {

    // ── UI components ──────────────────────────────────────────────────────────
    RecyclerView           recyclerView;
    SearchView             searchBar;
    BottomNavigationView   bottomNav;

    // ── Data layer ─────────────────────────────────────────────────────────────
    com.example.mobilemarketapp.ItemAdapter adapter;
    List<Item>     itemList;
    com.example.mobilemarketapp.DBHelper dbHelper; // single shared instance — passed into the adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise database helper (shared with the adapter to avoid
        // creating a new connection per list row)
        dbHelper = new DBHelper(this);

        // ── Bind views ────────────────────────────────────────────────────────
        recyclerView = findViewById(R.id.recyclerView);
        searchBar    = findViewById(R.id.searchBar);
        bottomNav    = findViewById(R.id.bottomNav);

        // ── RecyclerView setup ────────────────────────────────────────────────
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load all items from the database (sorted newest → oldest by datePosted)
        itemList = dbHelper.getAllItemsList();

        // Pass the shared dbHelper so the adapter can fetch average ratings
        // without opening a new database connection for every card
        adapter = new ItemAdapter(itemList, dbHelper);
        recyclerView.setAdapter(adapter);

        // ── Search bar ────────────────────────────────────────────────────────
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter on submit (keyboard action button)
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter live as the user types
                adapter.filter(newText);
                return false;
            }
        });

        // ── Bottom navigation ─────────────────────────────────────────────────
        bottomNav.setOnItemSelectedListener(navItem -> {
            int id = navItem.getItemId();

            if (id == R.id.nav_home) {
                // Already here — no action needed
                return true;

            } else if (id == R.id.nav_post) {
                startActivity(new Intent(this, PostItemActivity.class));
                return true;

            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    /**
     * Called every time this activity becomes visible again.
     * Refreshes the item list so newly posted items show up immediately
     * after returning from PostItemActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Re-query the database and push fresh data to the adapter
        itemList = dbHelper.getAllItemsList();
        adapter.updateList(itemList);
    }
}
