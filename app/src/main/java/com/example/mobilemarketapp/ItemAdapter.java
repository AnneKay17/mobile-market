package com.example.mobilemarketapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

//------CONNECTS ITEM_CARD.XML TO ITEM (BRIDGE BETWEEN UI AND DATA)

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    // List holds all items to display in RecyclerView (changes when searching)
    List<Item> itemList;

    //Full list (original data) used to restore items after filtering
    List<Item> itemListFull;

    // Constructor: receives data from MainActivity
    public ItemAdapter(List<Item> itemList){
        // stores data locally in adapter
        this.itemList = new ArrayList<>(itemList);

        //copy original data for filtering
        this.itemListFull = itemList;

    }
    public void refreshList(){

        itemList.clear();

        itemList.addAll(itemListFull);

        notifyDataSetChanged();
    }

    // View Holder: represents one row(card) in RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView price;
        TextView category;
        ImageView image;
        public ViewHolder(View view){
            super(view);

            //connect Java variables to xml views(item_card.xml
            name = view.findViewById(R.id.itemName);
            price = view.findViewById(R.id.itemPrice);
            category = view.findViewById(R.id.itemCategory);
            image = view.findViewById(R.id.itemImage);

        }
    }

    //Called when RecyclerView needs a new card(UI row)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        //Inflate: converts xml to actual View objext
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);

        return new ViewHolder(view); //returns card
    }

    //Called for each item to display data in UI
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        //get current item from list
        Item item = itemList.get(position);

        //set data into UI elements
        holder.name.setText(item.name);
        holder.price.setText("R" + item.price);
        holder.category.setText(item.category);

        //shows first image if available
        if(item.imageUris != null && !item.imageUris.isEmpty()){
            Uri uri = Uri.parse(item.imageUris.get(0)); //uses first image as preview image
            holder.image.setImageURI(uri);
        }

        // Makes items clickable
        holder.itemView.setOnClickListener(v -> {

            //create navigation to details screen
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);

            //send data to next screen
            intent.putExtra("name", item.name);
            intent.putExtra("description", item.description);
            intent.putExtra("price", item.price);
            intent.putExtra("seller", item.sellerName);
            intent.putExtra("category", item.category);
            intent.putStringArrayListExtra(
                    "images",
                    new ArrayList<>(item.imageUris)
            );

            //start new screen
            v.getContext().startActivity(intent);
        });
    }

    //tells RecyclerView how many items exist
    @Override
    public  int getItemCount(){
        return itemList.size();
    }

    //-------- Filters the list based on user input
    public void filter(String text) {

        // clears current displayed list
        itemList.clear();

        if (text.isEmpty()) {
            // If search is empty, show all items
            itemList.addAll(itemListFull);
        }
        else {
            //converts search text to lowercase for case-insensitive matching
            text = text.toLowerCase();

            //loop trough full list and find matches
            for (Item item : itemListFull) {

                //check if item name contains search text
                if (item.name.toLowerCase().contains(text)) {
                    itemList.add(item);
                }
            }
        }

        // refresh RecyclerView
        notifyDataSetChanged();
    }
}

//Item list(data) -> Adapter -> RecyclerView -> Screen