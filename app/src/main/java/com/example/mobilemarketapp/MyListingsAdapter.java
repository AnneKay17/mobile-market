package com.example.mobilemarketapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.ViewHolder> {

    List<Item> itemList;
    DBHelper dbHelper;
    String sellerName;
    Runnable onListChanged;

    public MyListingsAdapter(List<Item> itemList, DBHelper dbHelper, String sellerName, Runnable onListChanged) {
        this.itemList = itemList;
        this.dbHelper = dbHelper;
        this.sellerName = sellerName;
        this.onListChanged = onListChanged;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView listingName, listingPrice, listingStatus;
        Button markSoldBtn, deleteListingBtn;

        public ViewHolder(View view) {
            super(view);
            listingName = view.findViewById(R.id.listingName);
            listingPrice = view.findViewById(R.id.listingPrice);
            listingStatus = view.findViewById(R.id.listingStatus);
            markSoldBtn = view.findViewById(R.id.markSoldBtn);
            deleteListingBtn = view.findViewById(R.id.deleteListingBtn);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_listing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.listingName.setText(item.getName());
        holder.listingPrice.setText("R " + String.format("%.2f", item.getPrice()));

        if (item.isSold()) {
            holder.listingStatus.setText("Status: SOLD");
            holder.listingStatus.setTextColor(Color.RED);
            holder.markSoldBtn.setEnabled(false);
            holder.markSoldBtn.setText("Already Sold");
        } else {
            holder.listingStatus.setText("Status: Available");
            holder.listingStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.markSoldBtn.setEnabled(true);
            holder.markSoldBtn.setText("Mark as Sold");
        }

        holder.markSoldBtn.setOnClickListener(v -> {
            boolean success = dbHelper.markSellerItemAsSold(item.getItemId(), sellerName);

            if (success) {
                Toast.makeText(v.getContext(), "Item marked as sold", Toast.LENGTH_SHORT).show();
                if (onListChanged != null) onListChanged.run();
            } else {
                Toast.makeText(v.getContext(), "Could not mark item as sold", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteListingBtn.setOnClickListener(v -> {
            boolean success = dbHelper.deleteItem(item.getItemId(), sellerName);

            if (success) {
                Toast.makeText(v.getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                if (onListChanged != null) onListChanged.run();
            } else {
                Toast.makeText(v.getContext(), "Could not delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
