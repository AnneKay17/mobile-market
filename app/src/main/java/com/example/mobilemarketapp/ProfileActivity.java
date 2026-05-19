package com.example.mobilemarketapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    DBHelper dbHelper;

    ImageView profileImage;
    Button changeProfilePicBtn;

    ActivityResultLauncher<String[]> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);

        profileImage = findViewById(R.id.profileImage);

        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        TextView totalListingsText = findViewById(R.id.totalListingsText);
        TextView availableListingsText = findViewById(R.id.availableListingsText);
        TextView soldListingsText = findViewById(R.id.soldListingsText);

        Button myListingsBtn = findViewById(R.id.myListingsBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        changeProfilePicBtn = findViewById(R.id.changeProfilePicBtn);

        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);

        String username = prefs.getString("user", "Unknown User");
        String email = prefs.getString("email", "No email");

        String savedImageUri = prefs.getString("profileImage", null);

        if (savedImageUri != null) {
            try {
                profileImage.setImageURI(Uri.parse(savedImageUri));
            } catch (Exception e) {
                e.printStackTrace();
                prefs.edit().remove("profileImage").apply();
                profileImage.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }

                        profileImage.setImageURI(uri);

                        prefs.edit()
                                .putString("profileImage", uri.toString())
                                .apply();
                    }
                }
        );

        changeProfilePicBtn.setOnClickListener(v -> {
            imagePicker.launch(new String[]{"image/*"});
        });

        int totalListings = dbHelper.countItemsBySeller(username);
        int availableListings = dbHelper.countItemsBySellerAndStatus(username, "available");
        int soldListings = dbHelper.countItemsBySellerAndStatus(username, "sold");

        profileName.setText(username);
        profileEmail.setText(email);

        totalListingsText.setText("Total Listings: " + totalListings);
        availableListingsText.setText("Available: " + availableListings);
        soldListingsText.setText("Sold: " + soldListings);

        myListingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyListingsActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();

            editor.remove("user");
            editor.remove("email");
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}