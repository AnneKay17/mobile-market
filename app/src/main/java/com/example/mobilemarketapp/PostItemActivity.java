package com.example.mobilemarketapp;

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

import java.util.ArrayList;
import java.util.List;

public class PostItemActivity extends AppCompatActivity {
    EditText nameInput, descInput, priceInput;
    Spinner categorySpinner;
    Button postBtn, addImagesBtn;
    ActivityResultLauncher<String> imagePicker; //Image picker
    List<String> selectedImages = new ArrayList<>(); //image list

    RecyclerView previewRecycler;
    ImagePreviewAdapter previewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connects to layout
        setContentView(R.layout.activity_post_item);

        nameInput = findViewById(R.id.inputName);
        descInput = findViewById(R.id.inputDescription);
        priceInput = findViewById(R.id.inputPrice);
        addImagesBtn = findViewById(R.id.addImagesBtn);
        categorySpinner = findViewById(R.id.categorySpinner);
        postBtn = findViewById(R.id.btnPost);
        previewRecycler = findViewById(R.id.imagePreviewRecycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        previewRecycler.setLayoutManager(layoutManager);

        previewAdapter = new ImagePreviewAdapter(selectedImages);

        previewRecycler.setAdapter(previewAdapter);

        //Category options
        String[] categories = {
                "Select Category",
                "Electronics",
                "Clothing",
                "Books",
                "Furniture",
                "Food",
                "Other"
        };

        //adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        //connects adapter to spinner
        categorySpinner.setAdapter(adapter);

        String seller = getSharedPreferences("app", MODE_PRIVATE)
                .getString("user", "Unknown");

        //---- Image picker
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {

                    if (uris == null) return;

                    selectedImages.clear();

                    for (Uri uri : uris) {
                        final int takeFlags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION;

                        getContentResolver().takePersistableUriPermission(
                                uri,
                                takeFlags
                        );
                        selectedImages.add(uri.toString());
                    }
                    previewAdapter.notifyDataSetChanged();
                }
        );

        addImagesBtn.setOnClickListener(v -> {
            imagePicker.launch("image/*");
        });

        postBtn.setOnClickListener(view -> {

            String name = nameInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String priceText = priceInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            //--------USER INPUT VALIDATION----------//

            //Validates name
            if(name.isEmpty()){
                Toast.makeText(this,
                        "Enter item name",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //Validates description
            if(desc.isEmpty()){
                Toast.makeText(this,
                        "Enter description",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //check if price field is empty
            if(priceText.isEmpty()){
                Toast.makeText(this,
                        "Enter price",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //handles incorrect input
            double price;
            try {
                //converts price to double
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {

                Toast.makeText(this,
                        "Invalid price",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            //Ensures that user selects a category and not the "Select cate option"
            if(category.equals("Select Category")){
                Toast.makeText(this,
                        "Select a category",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //temporary date
            //CREATE HELPER METHOD FOR READABLE DATES
            String datePosted = String.valueOf(System.currentTimeMillis());

            //------CREATE NEW ITEM OBJECT-----//
            Item item = new Item(
                    name,
                    desc,
                    price,
                    seller,
                    datePosted,
                    category,
                    selectedImages
            );

            // add item to shared list
            // later replaced with:
            // dbHelper.insertItem(item)
            ItemStore.items.add(item);

            Toast.makeText(this, "Item posted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
