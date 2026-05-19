package com.example.mobilemarketapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostItemActivity extends AppCompatActivity {

    EditText nameInput, descInput, priceInput;
    Spinner categorySpinner;
    Button postBtn, addImagesBtn;
    ActivityResultLauncher<String> imagePicker;
    List<String> selectedImages = new ArrayList<>();
    RecyclerView previewRecycler;
    ImagePreviewAdapter previewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);

        nameInput       = findViewById(R.id.inputName);
        descInput       = findViewById(R.id.inputDescription);
        priceInput      = findViewById(R.id.inputPrice);
        addImagesBtn    = findViewById(R.id.addImagesBtn);
        categorySpinner = findViewById(R.id.categorySpinner);
        postBtn         = findViewById(R.id.btnPost);
        previewRecycler = findViewById(R.id.imagePreviewRecycler);

        previewRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        previewAdapter = new ImagePreviewAdapter(selectedImages);
        previewRecycler.setAdapter(previewAdapter);

        String[] categories = {"Select Category", "Electronics", "Clothing", "Books", "Furniture", "Food", "Other"};
        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        String sellerName = getSharedPreferences("app", MODE_PRIVATE).getString("user", "Unknown");

        imagePicker = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris == null || uris.isEmpty()) return;
            selectedImages.clear();
            for (Uri uri : uris) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                selectedImages.add(uri.toString());
            }
            previewAdapter.notifyDataSetChanged();
        });

        addImagesBtn.setOnClickListener(v -> imagePicker.launch("image/*"));

        postBtn.setOnClickListener(view -> {
            String name      = nameInput.getText().toString().trim();
            String desc      = descInput.getText().toString().trim();
            String priceText = priceInput.getText().toString().trim();
            String category  = categorySpinner.getSelectedItem().toString();

            if (name.isEmpty()) { Toast.makeText(this, "Enter an item name!", Toast.LENGTH_SHORT).show(); return; }
            if (desc.isEmpty()) { Toast.makeText(this, "Enter a description!", Toast.LENGTH_SHORT).show(); return; }
            if (priceText.isEmpty()) { Toast.makeText(this, "Enter a price!", Toast.LENGTH_SHORT).show(); return; }
            if (category.equals("Select Category")) { Toast.makeText(this, "Select a category!", Toast.LENGTH_SHORT).show(); return; }

            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price <= 0) { Toast.makeText(this, "Price must be greater than zero!", Toast.LENGTH_SHORT).show(); return; }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid price!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConnected()) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }

            String imagesStr = String.join(",", selectedImages);

            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("name",        name);
                    json.put("description", desc);
                    json.put("price",       price);
                    json.put("sellerName",  sellerName);
                    json.put("category",    category);
                    json.put("imageUris",   imagesStr);

                    String response = ApiClient.post("post_item.php", json.toString());
                    JSONObject obj  = new JSONObject(response);
                    boolean success = obj.getBoolean("success");

                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Item posted!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to post item!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
