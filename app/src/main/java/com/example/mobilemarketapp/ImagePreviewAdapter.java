package com.example.mobilemarketapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//Shows small image previews in PostItemActivity
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
    List<String> imageUris;

    //constructor
    public ImagePreviewAdapter(List<String> imageUris){
        this.imageUris = imageUris;
    }

    // ViewHolder for each image
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        public ViewHolder(View view){
            super(view);

            image = view.findViewById(R.id.previewImage);
        }
    }

    //creates image layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_preview_item, parent, false);

        return new ViewHolder(view);
    }

    //binds image to ImageView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Uri uri = Uri.parse(imageUris.get(position));
        holder.image.setImageURI(uri);
    }

    //Number of images
    @Override
    public int getItemCount(){
        return imageUris.size();
    }

}
