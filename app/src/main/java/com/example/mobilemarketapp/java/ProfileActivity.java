package com.example.mobilemarketapp.java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilemarketapp.LoginActivity;
import com.example.mobilemarketapp.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView profileImage = findViewById(R.id.profileImage);
        TextView profileName = findViewById(R.id.profileName);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        // Get stored username (from login/register)
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        String username = prefs.getString("user", "Unknown User");

        profileName.setText(username);

        // Logout logic
        logoutBtn.setOnClickListener(v -> {

            // clear stored session
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // go back to login
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}