package com.example.mobilemarketapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        // get data from previous screen (Intent)
        String itemName = getIntent().getStringExtra("name");
        double itemPrice = getIntent().getDoubleExtra("price", 0);
        String itemDesc = getIntent().getStringExtra("description");
        String itemSeller = getIntent().getStringExtra("seller");

        // displays data on screen
        name.setText(itemName);
        price.setText("R " + itemPrice);
        desc.setText(itemDesc);
        seller.setText("Seller: " + itemSeller);
    }
}
