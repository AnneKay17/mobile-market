package com.example.mobilemarketapp.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilemarketapp.Item;
import com.example.mobilemarketapp.R;

import java.util.List;

/**
 * CartAdapter — RecyclerView adapter for the shopping basket.
 *
 * Each row shows:
 *   - Item name
 *   - Seller name
 *   - Price in Rands
 *   - A "Remove" button that deletes the item from the basket
 *
 * A Runnable callback is invoked after every removal so CartActivity
 * can update the total price and check if the basket is now empty.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    // The shared cart list from CartStore
    List<com.example.mobilemarketapp.Item> cartItems;

    // Callback to notify CartActivity when an item is removed
    // so it can update the total and toggle the empty-state UI
    Runnable onCartChanged;

    /**
     * @param cartItems     The shared static cart list.
     * @param onCartChanged Runnable called after any item is removed.
     */
    public CartAdapter(List<com.example.mobilemarketapp.Item> cartItems, Runnable onCartChanged) {
        this.cartItems      = cartItems;
        this.onCartChanged  = onCartChanged;
    }

    // ── ViewHolder — caches view references per row ────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, sellerText, priceText;
        Button   removeBtn;

        public ViewHolder(View view) {
            super(view);
            nameText   = view.findViewById(R.id.cartItemName);
            sellerText = view.findViewById(R.id.cartItemSeller);
            priceText  = view.findViewById(R.id.cartItemPrice);
            removeBtn  = view.findViewById(R.id.removeBtn);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = cartItems.get(position);

        // Populate the card fields
        holder.nameText.setText(item.name);
        holder.sellerText.setText("Seller: " + item.sellerName);
        holder.priceText.setText("R " + String.format("%.2f", item.price));

        // Remove single item from the basket on button click
        holder.removeBtn.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return; // guard against stale positions

            cartItems.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, cartItems.size()); // shift remaining items up

            // Tell CartActivity to refresh the total and empty-state UI
            if (onCartChanged != null) {
                onCartChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
