package com.example.mobilemarketapp;

import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemDetailsActivity extends AppCompatActivity {

    TextView ratingText;
    ArrayList<String> commentsList = new ArrayList<>();
    ArrayAdapter<String> commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        TextView nameText        = findViewById(R.id.detailName);
        TextView priceText       = findViewById(R.id.detailPrice);
        TextView descText        = findViewById(R.id.detailDescription);
        TextView sellerText      = findViewById(R.id.detailSeller);
        TextView categoryText    = findViewById(R.id.detailCategory);
        ratingText               = findViewById(R.id.detailRating);

        ViewPager2 viewPager     = findViewById(R.id.viewPager);
        TabLayout tabDots        = findViewById(R.id.tabDots);

        Button addCartBtn        = findViewById(R.id.addToCartBtn);
        Button rateBtn           = findViewById(R.id.rateButton);
        EditText commentInput    = findViewById(R.id.commentInput);
        Button commentBtn        = findViewById(R.id.commentBtn);
        ListView commentListView = findViewById(R.id.commentList);

        String itemName             = getIntent().getStringExtra("name");
        double itemPrice            = getIntent().getDoubleExtra("price", 0.0);
        String itemDesc             = getIntent().getStringExtra("description");
        String itemSeller           = getIntent().getStringExtra("seller");
        String itemCategory         = getIntent().getStringExtra("category");
        ArrayList<String> imageUris = getIntent().getStringArrayListExtra("images");
        int itemId                  = getIntent().getIntExtra("itemId", -1);

        String loggedInUser  = getSharedPreferences("app", MODE_PRIVATE).getString("user", "Unknown");
        String loggedInEmail = getSharedPreferences("app", MODE_PRIVATE).getString("email", "");

        nameText.setText(itemName);
        priceText.setText("R " + String.format("%.2f", itemPrice));
        descText.setText(itemDesc);
        sellerText.setText("Seller: " + itemSeller);
        categoryText.setText(itemCategory);

        refreshRating(itemId);

        if (imageUris != null && !imageUris.isEmpty()) {
            viewPager.setAdapter(new ImagePagerAdapter(imageUris));
            new TabLayoutMediator(tabDots, viewPager, (tab, position) -> {}).attach();
        } else {
            viewPager.setVisibility(View.GONE);
            tabDots.setVisibility(View.GONE);
        }

        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentsList);
        commentListView.setAdapter(commentsAdapter);
        loadComments(itemId);

        commentBtn.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isConnected()) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("itemId",      itemId);
                    json.put("userName",    loggedInUser);
                    json.put("commentText", text);

                    String response = ApiClient.post("add_comment.php", json.toString());
                    JSONObject obj  = new JSONObject(response);

                    runOnUiThread(() -> {
                        if (obj.optBoolean("success")) {
                            commentInput.setText("");
                            loadComments(itemId);
                            Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        addCartBtn.setOnClickListener(v -> {
            if (itemId == -1) { Toast.makeText(this, "Invalid item!", Toast.LENGTH_SHORT).show(); return; }
            if (loggedInUser.equals(itemSeller)) { Toast.makeText(this, "You cannot buy your own product!", Toast.LENGTH_SHORT).show(); return; }
            if (!isConnected()) { Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show(); return; }

            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("userEmail", loggedInEmail);
                    json.put("itemId",    itemId);

                    String response = ApiClient.post("add_to_cart.php", json.toString());
                    JSONObject obj  = new JSONObject(response);

                    runOnUiThread(() ->
                            Toast.makeText(this, obj.optString("message"), Toast.LENGTH_SHORT).show()
                    );
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        rateBtn.setOnClickListener(v -> {
            if (itemId == -1) { Toast.makeText(this, "Cannot rate this item!", Toast.LENGTH_SHORT).show(); return; }
            if (!isConnected()) { Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show(); return; }

            // Build the RatingBar properly so exactly 5 stars show and all are tappable
            RatingBar ratingBar = new RatingBar(this);
            ratingBar.setNumStars(5);
            ratingBar.setMax(5);
            ratingBar.setStepSize(1f);
            ratingBar.setRating(0);

            // Wrap in a LinearLayout with padding so it fits nicely in the dialog
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 10);

            // Measure the star size to fit 5 stars inside the dialog width
            int starSize = (int) (getResources().getDisplayMetrics().widthPixels * 0.55f / 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    starSize * 5,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ratingBar.setLayoutParams(params);
            layout.addView(ratingBar);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rate \"" + itemName + "\"");
            builder.setView(layout);

            builder.setPositiveButton("Submit", (dialog, which) -> {
                int stars = (int) ratingBar.getRating();
                if (stars == 0) { Toast.makeText(this, "Please select at least 1 star!", Toast.LENGTH_SHORT).show(); return; }

                new Thread(() -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("itemId",   itemId);
                        json.put("userName", loggedInUser);
                        json.put("rating",   stars);

                        String response = ApiClient.post("add_rating.php", json.toString());
                        JSONObject obj  = new JSONObject(response);
                        boolean saved   = obj.getBoolean("success");

                        runOnUiThread(() -> {
                            if (saved) {
                                refreshRating(itemId);
                                Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "You already rated this item!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void refreshRating(int itemId) {
        new Thread(() -> {
            try {
                String response = ApiClient.get("get_average_rating.php?itemId=" + itemId);
                JSONObject obj  = new JSONObject(response);
                double avg      = obj.optDouble("average", 0.0);

                runOnUiThread(() -> {
                    if (avg == 0) {
                        ratingText.setText("Average Rating: No ratings yet");
                    } else {
                        ratingText.setText("Average Rating: ⭐ " + String.format("%.1f", avg) + " / 5");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ratingText.setText("Average Rating: unavailable"));
            }
        }).start();
    }

    private void loadComments(int itemId) {
        new Thread(() -> {
            try {
                String response = ApiClient.get("get_comments.php?itemId=" + itemId);
                JSONObject obj  = new JSONObject(response);

                if (!obj.has("comments")) return;

                JSONArray arr = obj.getJSONArray("comments");
                ArrayList<String> loaded = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    loaded.add(c.getString("username") + ": " + c.getString("commenttext"));
                }

                runOnUiThread(() -> {
                    commentsList.clear();
                    commentsList.addAll(loaded);
                    commentsAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
