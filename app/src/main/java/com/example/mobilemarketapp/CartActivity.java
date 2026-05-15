package com.example.mobilemarketapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        RecyclerView recyclerView = findViewById(R.id.cartRecycler);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        ItemAdapter adapter = new ItemAdapter(CartStore.cartItems);

        recyclerView.setAdapter(adapter);
    }
}