package com.example.mobilemarketapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    List<Item> cartItems;

    public interface RemoveCallback {
        void onRemove(Item item, int position);
    }

    RemoveCallback removeCallback;

    public CartAdapter(List<Item> cartItems, RemoveCallback removeCallback) {
        this.cartItems      = cartItems;
        this.removeCallback = removeCallback;
    }


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


        holder.nameText.setText(item.name);
        holder.sellerText.setText("Seller: " + item.sellerName);
        holder.priceText.setText("R " + String.format("%.2f", item.price));

        holder.removeBtn.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (removeCallback != null) {
                removeCallback.onRemove(cartItems.get(pos), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
