package com.example.mobilemarketapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    RecyclerView cartRecycler;
    CartAdapter  adapter;
    TextView     emptyText, totalPriceText;
    Button       clearCartBtn, checkoutBtn;

    ArrayList<Item> cartItems = new ArrayList<>();
    String loggedInEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        loggedInEmail = getSharedPreferences("app", MODE_PRIVATE).getString("email", "");

        cartRecycler   = findViewById(R.id.cartRecycler);
        emptyText      = findViewById(R.id.emptyText);
        totalPriceText = findViewById(R.id.totalPriceText);
        clearCartBtn   = findViewById(R.id.clearCartBtn);
        checkoutBtn    = findViewById(R.id.checkoutBtn);

        cartRecycler.setLayoutManager(new LinearLayoutManager(this));

        if (!isConnected()) {
            emptyText.setText("No internet connection!");
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        loadCartFromServer();

        clearCartBtn.setOnClickListener(v -> {
            if (cartItems.isEmpty()) return;
            if (!isConnected()) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                try {
                    for (Item item : new ArrayList<>(cartItems)) {
                        JSONObject json = new JSONObject();
                        json.put("userEmail", loggedInEmail);
                        json.put("itemId", item.getItemId());
                        ApiClient.post("remove_from_cart.php", json.toString());
                    }
                    runOnUiThread(() -> {
                        cartItems.clear();
                        if (adapter != null) adapter.notifyDataSetChanged();
                        updateUI();
                        Toast.makeText(this, "Basket cleared!", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });

        checkoutBtn.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your basket is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isConnected()) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
        });
    }

    private void loadCartFromServer() {
        new Thread(() -> {
            try {
                String response = ApiClient.get("get_cart.php?userEmail=" + loggedInEmail);
                JSONObject obj  = new JSONObject(response);

                if (!obj.has("items")) {
                    runOnUiThread(this::updateUI);
                    return;
                }

                JSONArray arr = obj.getJSONArray("items");
                ArrayList<Item> loaded = new ArrayList<>();

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
                    cartItems.clear();
                    cartItems.addAll(loaded);
                    adapter = new CartAdapter(cartItems, this::onItemRemoved);
                    cartRecycler.setAdapter(adapter);
                    updateUI();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    emptyText.setText("Could not load basket. Check your connection.");
                    emptyText.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    public void onItemRemoved(Item item, int position) {
        if (!isConnected()) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("userEmail", loggedInEmail);
                json.put("itemId", item.getItemId());

                String response = ApiClient.post("remove_from_cart.php", json.toString());
                JSONObject obj  = new JSONObject(response);

                runOnUiThread(() -> {
                    if (obj.optBoolean("success", false)) {
                        if (position < cartItems.size()) {
                            cartItems.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, cartItems.size());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Could not remove item!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    public void updateUI() {
        boolean empty = cartItems == null || cartItems.isEmpty();
        emptyText.setText("Your basket is empty!");
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        cartRecycler.setVisibility(empty ? View.GONE  : View.VISIBLE);
        checkoutBtn.setVisibility(empty ? View.GONE   : View.VISIBLE);
        clearCartBtn.setVisibility(empty ? View.GONE  : View.VISIBLE);

        if (!empty) {
            double total = 0;
            for (Item item : cartItems) total += item.price;
            totalPriceText.setText("Total: R " + String.format("%.2f", total));
            totalPriceText.setVisibility(View.VISIBLE);
        } else {
            totalPriceText.setVisibility(View.GONE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
