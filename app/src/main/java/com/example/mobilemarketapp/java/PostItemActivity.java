package com.example.mobilemarketapp.java;

import android.content.Intent;
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

import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.ImagePreviewAdapter;
import com.example.mobilemarketapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * PostItemActivity — Screen for listing a new item for sale.
 *
 * The seller can:
 *   - Pick multiple images from the device gallery
 *   - Enter item name, description, price
 *   - Choose a category from a dropdown spinner
 *
 * On submit, the item is saved to the SQLite database via DBHelper.insertItem().
 * The seller name is read from SharedPreferences (set during login/register).
 *
 * Validation checks:
 *   - Name must not be empty
 *   - Description must not be empty
 *   - Price must be a valid number
 *   - A category must be selected (not the placeholder)
 */
public class PostItemActivity extends AppCompatActivity {

    // ── Input views ───────────────────────────────────────────────────────────
    EditText  nameInput, descInput, priceInput;
    Spinner   categorySpinner;
    Button    postBtn, addImagesBtn;

    // ── Image picker ──────────────────────────────────────────────────────────
    ActivityResultLauncher<String> imagePicker; // system gallery picker
    List<String>       selectedImages = new ArrayList<>(); // URIs of chosen images
    RecyclerView       previewRecycler;
    com.example.mobilemarketapp.ImagePreviewAdapter previewAdapter;

    // Database helper
    com.example.mobilemarketapp.DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);

        // Initialise database helper
        dbHelper = new DBHelper(this);

        // ── Bind views ────────────────────────────────────────────────────────
        nameInput      = findViewById(R.id.inputName);
        descInput      = findViewById(R.id.inputDescription);
        priceInput     = findViewById(R.id.inputPrice);
        addImagesBtn   = findViewById(R.id.addImagesBtn);
        categorySpinner = findViewById(R.id.categorySpinner);
        postBtn        = findViewById(R.id.btnPost);
        previewRecycler = findViewById(R.id.imagePreviewRecycler);

        // ── Image preview RecyclerView (horizontal scroll) ────────────────────
        previewRecycler.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        previewAdapter = new ImagePreviewAdapter(selectedImages);
        previewRecycler.setAdapter(previewAdapter);

        // ── Category spinner ──────────────────────────────────────────────────
        String[] categories = {
            "Select Category", // placeholder — user must pick a real one
            "Electronics",
            "Clothing",
            "Books",
            "Furniture",
            "Food",
            "Other"
        };
        categorySpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        ));

        // ── Read logged-in seller name from session ────────────────────────────
        String sellerName = getSharedPreferences("app", MODE_PRIVATE)
            .getString("user", "Unknown");

        // ── Image picker — opens device gallery, allows multiple selections ────
        imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris == null || uris.isEmpty()) return;
                selectedImages.clear();
                for (Uri uri : uris) {
                    // Take a persistent URI permission so the app can read the
                    // image even after the session ends
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    selectedImages.add(uri.toString());
                }
                previewAdapter.notifyDataSetChanged(); // refresh the thumbnail strip
            }
        );

        // Open gallery when "Add Images" is tapped
        addImagesBtn.setOnClickListener(v -> imagePicker.launch("image/*"));

        // ── Post button ───────────────────────────────────────────────────────
        postBtn.setOnClickListener(view -> {
            String name      = nameInput.getText().toString().trim();
            String desc      = descInput.getText().toString().trim();
            String priceText = priceInput.getText().toString().trim();
            String category  = categorySpinner.getSelectedItem().toString();

            // Validate: name required
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter an item name!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: description required
            if (desc.isEmpty()) {
                Toast.makeText(this, "Enter a description!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: price required and must be a valid number
            if (priceText.isEmpty()) {
                Toast.makeText(this, "Enter a price!", Toast.LENGTH_SHORT).show();
                return;
            }
            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    Toast.makeText(this, "Price cannot be negative!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid price!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: a real category must be chosen
            if (category.equals("Select Category")) {
                Toast.makeText(this, "Please select a category!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Join image URIs into a single comma-separated string for storage
            String imagesStr = String.join(",", selectedImages);

            // Insert into SQLite — datePosted is set automatically inside insertItem()
            boolean success = dbHelper.insertItem(name, desc, price, sellerName, category, imagesStr);

            if (success) {
                Toast.makeText(this, "Item posted successfully!", Toast.LENGTH_SHORT).show();
                finish(); // return to MainActivity (onResume will refresh the list)
            } else {
                Toast.makeText(this, "Failed to post item. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
