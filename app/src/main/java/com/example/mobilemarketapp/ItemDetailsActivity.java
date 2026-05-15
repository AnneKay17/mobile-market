package com.example.mobilemarketapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

//---------------------- ITEM DETAIL SCREEN-------------------------//
public class ItemDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //connect this to activity_item_details.xml
        setContentView(R.layout.activity_item_details);

        // find UI elements in xml
        TextView name = findViewById(R.id.detailName);
        TextView price = findViewById(R.id.detailPrice);
        TextView desc = findViewById(R.id.detailDescription);
        TextView seller = findViewById(R.id.detailSeller);
        TextView category = findViewById(R.id.detailCategory);
        TextView rating = findViewById(R.id.detailRating);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabDots = findViewById(R.id.tabDots);
        Button addToCartBtn = findViewById(R.id.addToCartBtn);


        // get data from previous screen (Intent)
        String itemName = getIntent().getStringExtra("name");
        double itemPrice = getIntent().getDoubleExtra("price", 0);
        String itemDesc = getIntent().getStringExtra("description");
        String itemSeller = getIntent().getStringExtra("seller");
        String itemCategory = getIntent().getStringExtra("category");
        double itemRating = getIntent().getDoubleExtra("rating", 0);
        ArrayList<String> imageUris = getIntent().getStringArrayListExtra("images");

        addToCartBtn.setOnClickListener(v -> {

            Item item = new Item(
                    itemName,
                    itemDesc,
                    itemPrice,
                    itemSeller,
                    "",
                    itemCategory,
                    imageUris
            );

            CartStore.cartItems.add(item);

            Toast.makeText(this,
                    "Added to basket",
                    Toast.LENGTH_SHORT).show();
        });


        // displays data on screen
        name.setText(itemName);
        price.setText("R " + itemPrice);
        desc.setText(itemDesc);
        seller.setText("Seller: " + itemSeller);
        category.setText(itemCategory);
        rating.setText("Average Rating" + itemRating);

        /// Setup image swipe gallery
        if (imageUris != null && !imageUris.isEmpty()) {

            ImagePagerAdapter adapter = new ImagePagerAdapter(imageUris);
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabDots, viewPager,
                    (tab, position) -> {
                        // dots only
                    }
            ).attach();

        }
        else {
            tabDots.setVisibility(View.GONE);
        }
    }
}
