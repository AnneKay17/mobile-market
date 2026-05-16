package com.example.mobilemarketapp.java;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilemarketapp.DBHelper;
import com.example.mobilemarketapp.Item;
import com.example.mobilemarketapp.ItemDetailsActivity;
import com.example.mobilemarketapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemAdapter — RecyclerView adapter that displays marketplace listings.
 *
 * Each card shows:
 *   - Thumbnail (first image URI if available)
 *   - Item name
 *   - Category badge
 *   - Average star rating (fetched from the shared DBHelper)
 *   - Price in Rands
 *
 * Tapping a card opens ItemDetailsActivity with all item data passed as extras.
 *
 * The adapter keeps two lists:
 *   - itemList     : the currently displayed/filtered list
 *   - itemListFull : the complete unfiltered list (for restoring after search)
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    // Currently displayed items (may be a filtered subset of itemListFull)
    List<com.example.mobilemarketapp.Item> itemList;

    // Full unfiltered list — used to restore results when the search query is cleared
    List<com.example.mobilemarketapp.Item> itemListFull;

    // Shared database helper — passed in from the Activity to avoid creating
    // a new connection on every single row bind (which was the previous bug)
    com.example.mobilemarketapp.DBHelper dbHelper;

    /**
     * Constructor — receives both the item data and a shared database connection.
     *
     * @param itemList The initial list of items to display.
     * @param dbHelper A DBHelper instance shared from the hosting Activity.
     */
    public ItemAdapter(List<com.example.mobilemarketapp.Item> itemList, DBHelper dbHelper) {
        this.itemList     = new ArrayList<>(itemList);
        this.itemListFull = new ArrayList<>(itemList);
        this.dbHelper     = dbHelper;
    }

    // ── ViewHolder — caches view references so findViewById isn't called repeatedly ─

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  nameText, priceText, categoryText, ratingText;
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            nameText     = view.findViewById(R.id.itemName);
            priceText    = view.findViewById(R.id.itemPrice);
            categoryText = view.findViewById(R.id.itemCategory);
            ratingText   = view.findViewById(R.id.itemRating);
            imageView    = view.findViewById(R.id.itemImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item_card layout for each row
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        com.example.mobilemarketapp.Item item = itemList.get(position);

        // ── Bind data to the card views ───────────────────────────────────────
        holder.nameText.setText(item.name);
        holder.priceText.setText("R " + String.format("%.2f", item.price));
        holder.categoryText.setText(item.category);

        // Fetch average rating using the shared DBHelper (not a new instance per row)
        double avg = dbHelper.getAverageRating(item.itemId);
        if (avg == 0.0) {
            holder.ratingText.setText("⭐ No ratings yet");
        } else {
            holder.ratingText.setText("⭐ " + String.format("%.1f", avg) + "/5");
        }

        // Show the first image if this item has any
        if (item.imageUris != null && !item.imageUris.isEmpty()) {
            holder.imageView.setImageURI(Uri.parse(item.imageUris.get(0)));
        } else {
            // Reset to the default placeholder so recycled views don't show old images
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // ── Card click → open ItemDetailsActivity ─────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra("itemId",      item.itemId);
            intent.putExtra("name",        item.name);
            intent.putExtra("description", item.description);
            intent.putExtra("price",       item.price);
            intent.putExtra("seller",      item.sellerName);
            intent.putExtra("category",    item.category);
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

    // ── List management methods ───────────────────────────────────────────────

    /**
     * Replaces both lists with fresh data from the database.
     * Called from MainActivity.onResume() after posting a new item.
     *
     * @param newList Updated item list.
     */
    public void updateList(List<com.example.mobilemarketapp.Item> newList) {
        itemListFull.clear();
        itemListFull.addAll(newList);
        itemList.clear();
        itemList.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * Filters the displayed list to items whose name contains the query.
     * If the query is empty, all items are shown again.
     *
     * @param query Text typed in the SearchView.
     */
    public void filter(String query) {
        itemList.clear();
        if (query.isEmpty()) {
            // Restore the full list when the search bar is cleared
            itemList.addAll(itemListFull);
        } else {
            String lower = query.toLowerCase();
            for (Item item : itemListFull) {
                if (item.name.toLowerCase().contains(lower)) {
                    itemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
