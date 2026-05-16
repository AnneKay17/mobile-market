package com.example.mobilemarketapp.java;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilemarketapp.CartAdapter;
import com.example.mobilemarketapp.CartStore;
import com.example.mobilemarketapp.Item;
import com.example.mobilemarketapp.R;

/**
 * CartActivity — The shopping basket screen.
 *
 * Displays:
 *   - A list of items the user has added to their basket
 *   - A running total price
 *   - A "Checkout" button that simulates placing an order
 *   - A "Clear Basket" button that removes all items
 *
 * CartStore.cartItems is a static list shared across the whole app.
 * Items are added from ItemDetailsActivity and removed here.
 */
public class CartActivity extends AppCompatActivity {

    // ── UI views ──────────────────────────────────────────────────────────────
    RecyclerView cartRecycler;
    com.example.mobilemarketapp.CartAdapter adapter;
    TextView     emptyText;
    TextView     totalPriceText;  // shows the running total
    Button       clearCartBtn;
    Button       checkoutBtn;     // new checkout button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // ── Bind views ────────────────────────────────────────────────────────
        cartRecycler   = findViewById(R.id.cartRecycler);
        emptyText      = findViewById(R.id.emptyText);
        totalPriceText = findViewById(R.id.totalPriceText);
        clearCartBtn   = findViewById(R.id.clearCartBtn);
        checkoutBtn    = findViewById(R.id.checkoutBtn);

        // ── RecyclerView setup ────────────────────────────────────────────────
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Pass a callback so the adapter can notify us when an item is removed
        // so we can update the total price and empty-state visibility
        adapter = new CartAdapter(com.example.mobilemarketapp.CartStore.cartItems, this::onCartChanged);
        cartRecycler.setAdapter(adapter);

        // Show the correct UI state on first open
        onCartChanged();

        // ── Clear basket ──────────────────────────────────────────────────────
        clearCartBtn.setOnClickListener(v -> {
            com.example.mobilemarketapp.CartStore.cartItems.clear();
            adapter.notifyDataSetChanged();
            onCartChanged();
            Toast.makeText(this, "Basket cleared!", Toast.LENGTH_SHORT).show();
        });

        // ── Checkout ──────────────────────────────────────────────────────────
        checkoutBtn.setOnClickListener(v -> {
            if (com.example.mobilemarketapp.CartStore.cartItems.isEmpty()) {
                Toast.makeText(this, "Your basket is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show a confirmation dialog before "placing" the order
            double total = calculateTotal();
            new AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("Place order for " + com.example.mobilemarketapp.CartStore.cartItems.size() +
                            " item(s)?\n\nTotal: R " + String.format("%.2f", total))
                .setPositiveButton("Place Order", (dialog, which) -> {
                    // Clear the basket after checkout
                    com.example.mobilemarketapp.CartStore.cartItems.clear();
                    adapter.notifyDataSetChanged();
                    onCartChanged();
                    Toast.makeText(this, "Order placed! Thank you 🎉", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    /**
     * Called whenever an item is added or removed from the cart.
     * Updates the total price, the empty-state text, and button visibility.
     */
    public void onCartChanged() {
        boolean empty = com.example.mobilemarketapp.CartStore.cartItems.isEmpty();

        // Toggle between the item list and "empty" message
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        cartRecycler.setVisibility(empty ? View.GONE  : View.VISIBLE);

        // Only show checkout and clear buttons when there are items
        checkoutBtn.setVisibility(empty ? View.GONE : View.VISIBLE);
        clearCartBtn.setVisibility(empty ? View.GONE : View.VISIBLE);

        // Update the total price label
        if (!empty) {
            double total = calculateTotal();
            totalPriceText.setText("Total: R " + String.format("%.2f", total));
            totalPriceText.setVisibility(View.VISIBLE);
        } else {
            totalPriceText.setVisibility(View.GONE);
        }
    }

    /**
     * Sums the price of every item currently in the basket.
     *
     * @return Total price as a double.
     */
    private double calculateTotal() {
        double total = 0.0;
        for (Item item : CartStore.cartItems) {
            total += item.price;
        }
        return total;
    }
}
