package com.example.mobilemarketapp.java;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilemarketapp.R;

import java.util.List;

// Adapter for swipeable item images
public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>{

    //Stores all image URIs
    List<String> images;

    //constructor
    public ImagePagerAdapter(List<String> images){
        this.images = images;
    }

    //ViewHolder for each image
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        public ViewHolder(View view){
            super(view);

            image = view.findViewById(R.id.imageView);
        }
    }

    //Creates image layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from((parent.getContext()))
                .inflate(R.layout.image_slide, parent, false);

        return new ViewHolder(view);
    }

    //binds image to imageView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Uri uri = Uri.parse(images.get(position));
        holder.image.setImageURI(uri);

    }

    //number of images
    @Override
    public int getItemCount(){
        return images.size();
    }
}
