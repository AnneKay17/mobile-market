package com.example.mobilemarketapp.java;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilemarketapp.CartStore;
import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.ImagePagerAdapter;
import com.example.mobilemarketapp.Item;
import com.example.mobilemarketapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

/**
 * ItemDetailsActivity — Full product detail screen.
 *
 * Displays:
 *   - Swipeable image gallery (ViewPager2 + dot indicators)
 *   - Item name, category, price, seller name
 *   - Average star rating (loaded from database)
 *   - Comments section (stored in-memory for this session)
 *   - Rate Item button (enforces one-rating-per-user via DBHelper)
 *   - Add To Basket button
 *
 * Data arrives via Intent extras put by ItemAdapter when the user taps a card.
 */
public class ItemDetailsActivity extends AppCompatActivity {

    // Database helper — used for ratings
    com.example.mobilemarketapp.DBHelper dbHelper;

    // Rating display (updated after a new rating is submitted)
    TextView ratingText;

    // Comments stored in-memory for this session
    ArrayList<String> commentsList;
    ArrayAdapter<String> commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialise database helper
        dbHelper = new DBHelper(this);

        // ── Bind all UI views ─────────────────────────────────────────────────
        TextView nameText     = findViewById(R.id.detailName);
        TextView priceText    = findViewById(R.id.detailPrice);
        TextView descText     = findViewById(R.id.detailDescription);
        TextView sellerText   = findViewById(R.id.detailSeller);
        TextView categoryText = findViewById(R.id.detailCategory);
        ratingText            = findViewById(R.id.detailRating);

        ViewPager2  viewPager  = findViewById(R.id.viewPager);
        TabLayout   tabDots    = findViewById(R.id.tabDots);
        Button      addCartBtn = findViewById(R.id.addToCartBtn);
        Button      rateBtn    = findViewById(R.id.rateButton);

        // ── Comment views — must be bound to avoid NullPointerException crash ─
        EditText commentInput   = findViewById(R.id.commentInput);
        Button   commentBtn     = findViewById(R.id.commentBtn);
        ListView commentListView = findViewById(R.id.commentList);

        // ── Read data passed in from ItemAdapter ──────────────────────────────
        String            itemName     = getIntent().getStringExtra("name");
        double            itemPrice    = getIntent().getDoubleExtra("price", 0.0);
        String            itemDesc     = getIntent().getStringExtra("description");
        String            itemSeller   = getIntent().getStringExtra("seller");
        String            itemCategory = getIntent().getStringExtra("category");
        ArrayList<String> imageUris    = getIntent().getStringArrayListExtra("images");
        int               itemId       = getIntent().getIntExtra("itemId", -1);

        // Get the currently logged-in username (used for rating ownership checks)
        String loggedInUser = getSharedPreferences("app", MODE_PRIVATE)
            .getString("user", "Unknown");

        // ── Populate text fields ──────────────────────────────────────────────
        nameText.setText(itemName);
        priceText.setText("R " + String.format("%.2f", itemPrice));
        descText.setText(itemDesc);
        sellerText.setText("Seller: " + itemSeller);
        categoryText.setText(itemCategory);

        // Load and display average rating from the database
        refreshRating(itemId);

        // ── Image gallery setup ───────────────────────────────────────────────
        if (imageUris != null && !imageUris.isEmpty()) {
            // Show swipeable image carousel with dot page indicators
            com.example.mobilemarketapp.ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(imageUris);
            viewPager.setAdapter(pagerAdapter);

            // Attach dot indicators to the ViewPager
            new TabLayoutMediator(tabDots, viewPager, (tab, position) -> {
                // tabs are used as dots only — no text needed
            }).attach();
        } else {
            // No images — hide the gallery and dots
            viewPager.setVisibility(View.GONE);
            tabDots.setVisibility(View.GONE);
        }

        // ── Comments setup ────────────────────────────────────────────────────
        // Comments are stored in-memory (not in DB) for this session
        commentsList    = new ArrayList<>();
        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentsList);
        commentListView.setAdapter(commentsAdapter);

        // Post a comment when the button is tapped
        commentBtn.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Prepend username so you know who wrote it
            commentsList.add(loggedInUser + ": " + text);
            commentsAdapter.notifyDataSetChanged();
            commentInput.setText(""); // clear the input field after posting
        });

        // ── Add to basket button ──────────────────────────────────────────────
        addCartBtn.setOnClickListener(v -> {
            // Create an Item object and add it to the shared static cart list
            com.example.mobilemarketapp.Item cartItem = new Item(
                itemName,
                itemDesc,
                itemPrice,
                itemSeller,
                itemCategory,
                imageUris != null ? imageUris : new ArrayList<>()
            );
            CartStore.cartItems.add(cartItem);
            Toast.makeText(this, itemName + " added to basket!", Toast.LENGTH_SHORT).show();
        });

        // ── Rate item button ──────────────────────────────────────────────────
        rateBtn.setOnClickListener(v -> {
            if (itemId == -1) {
                Toast.makeText(this, "Cannot rate this item!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build a dialog with a star RatingBar for the user to pick 1–5 stars
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rate \"" + itemName + "\"");

            RatingBar ratingBar = new RatingBar(this);
            ratingBar.setNumStars(5);
            ratingBar.setStepSize(1f);
            builder.setView(ratingBar);

            // "Submit" — attempt to save the rating
            builder.setPositiveButton("Submit", (dialog, which) -> {
                int stars = (int) ratingBar.getRating();

                if (stars == 0) {
                    Toast.makeText(this, "Please select at least 1 star!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // addRating() returns false if the user already rated this item
                boolean saved = dbHelper.addRating(itemId, loggedInUser, stars);

                if (saved) {
                    refreshRating(itemId); // update the displayed average
                    Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You already rated this item!", Toast.LENGTH_SHORT).show();
                }
            });

            // "Cancel" — dismiss the dialog with no action
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    /**
     * Reads the average rating for the given item from the database
     * and updates the ratingText TextView.
     *
     * @param itemId The item whose rating is displayed.
     */
    private void refreshRating(int itemId) {
        if (itemId == -1) {
            ratingText.setText("Average Rating: No ratings yet");
            return;
        }
        double avg = dbHelper.getAverageRating(itemId);
        if (avg == 0.0) {
            ratingText.setText("Average Rating: No ratings yet");
        } else {
            ratingText.setText("Average Rating: ⭐ " + String.format("%.1f", avg) + " / 5");
        }
    }
}
