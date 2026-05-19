package com.example.mobilemarketapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SearchView searchBar;
    Spinner categoryFilterSpinner;
    BottomNavigationView bottomNav;
    TextView emptyItemsText;

    ItemAdapter adapter;
    List<Item> itemList = new ArrayList<>();

    String currentSearch   = "";
    String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView          = findViewById(R.id.recyclerView);
        searchBar             = findViewById(R.id.searchBar);
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        bottomNav             = findViewById(R.id.bottomNav);
        emptyItemsText        = findViewById(R.id.emptyItemsText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(adapter);

        String[] categories = {"All", "Electronics", "Clothing", "Books", "Furniture", "Food", "Other"};
        categoryFilterSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query.trim();
                applyFilters();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText.trim();
                applyFilters();
                return false;
            }
        });

        bottomNav.setOnItemSelectedListener(navItem -> {
            int id = navItem.getItemId();
            if (id == R.id.nav_home) {
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

        loadItemsFromServer();
    }

    private void loadItemsFromServer() {
        if (!isConnected()) {
            emptyItemsText.setText("No internet connection!");
            emptyItemsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        new Thread(() -> {
            try {
                String response = ApiClient.get("get_items.php");
                JSONObject obj  = new JSONObject(response);

                if (!obj.optBoolean("success", false) || !obj.has("items")) {
                    runOnUiThread(() -> {
                        emptyItemsText.setText("No items found");
                        applyFilters();
                    });
                    return;
                }

                JSONArray arr = obj.getJSONArray("items");
                List<Item> loaded = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    ArrayList<String> images = new ArrayList<>();
                    String uris = item.optString("imageuris", "");
                    if (!uris.isEmpty()) {
                        for (String u : uris.split(",")) images.add(u.trim());
                    }
                    loaded.add(new Item(
                            item.getInt("itemid"),
                            item.getString("name"),
                            item.optString("description", ""),
                            item.getDouble("price"),
                            item.optString("sellername", ""),
                            item.optLong("dateposted", 0),
                            item.optString("category", ""),
                            images,
                            item.optString("status", "available")
                    ));
                }

                runOnUiThread(() -> {
                    emptyItemsText.setText("No items found");
                    itemList.clear();
                    itemList.addAll(loaded);
                    applyFilters();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    emptyItemsText.setText("Could not load items. Check your connection.");
                    emptyItemsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void applyFilters() {
        ArrayList<Item> filtered = new ArrayList<>();
        for (Item item : itemList) {
            boolean matchSearch   = currentSearch.isEmpty()
                    || item.getName().toLowerCase().contains(currentSearch.toLowerCase());
            boolean matchCategory = currentCategory.equals("All")
                    || item.getCategory().equalsIgnoreCase(currentCategory);
            if (matchSearch && matchCategory) filtered.add(item);
        }
        adapter.updateList(filtered);
        boolean empty = filtered.isEmpty();
        emptyItemsText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItemsFromServer();
    }
}
