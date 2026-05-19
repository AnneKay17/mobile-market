package com.example.mobilemarketapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    List<Item> itemList;
    List<Item> itemListFull;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = new ArrayList<>(itemList);
        this.itemListFull = new ArrayList<>(itemList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, categoryText, ratingText;
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.itemName);
            priceText = view.findViewById(R.id.itemPrice);
            categoryText = view.findViewById(R.id.itemCategory);
            ratingText = view.findViewById(R.id.itemRating);
            imageView = view.findViewById(R.id.itemImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.nameText.setText(item.name);
        holder.categoryText.setText(item.category);

        if (item.isSold()) {
            holder.priceText.setText("SOLD OUT");
            holder.priceText.setTextColor(Color.RED);
            holder.itemView.setAlpha(0.55f);
        } else {
            holder.priceText.setText("R " + String.format("%.2f", item.price));
            holder.priceText.setTextColor(Color.parseColor("#222222"));
            holder.itemView.setAlpha(1.0f);
        }

        holder.ratingText.setText("⭐ Tap to see rating");

        if (item.imageUris != null && !item.imageUris.isEmpty()) {
            try {
                holder.imageView.setImageURI(Uri.parse(item.imageUris.get(0)));
            } catch (Exception e) {
                holder.imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra("itemId", item.itemId);
            intent.putExtra("name", item.name);
            intent.putExtra("description", item.description);
            intent.putExtra("price", item.price);
            intent.putExtra("seller", item.sellerName);
            intent.putExtra("category", item.category);
            intent.putExtra("status", item.status);

            intent.putStringArrayListExtra(
                    "images",
                    new ArrayList<>(item.imageUris != null ? item.imageUris : new ArrayList<>())
            );

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<Item> newList) {
        itemListFull.clear();
        itemListFull.addAll(newList);

        itemList.clear();
        itemList.addAll(newList);

        notifyDataSetChanged();
    }
}