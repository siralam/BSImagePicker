package com.asksira.imagerpickersheet;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * The RecyclerView's adapter of the selectable image tiles.
 */

public class ImageTileAdapter extends RecyclerView.Adapter<ImageTileAdapter.ImageTileViewHolder> {

    protected Context context;

    public ImageTileAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public ImageTileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_tile, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageTileViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    public class ImageTileViewHolder extends RecyclerView.ViewHolder{

        ImageView image;

        public ImageTileViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_imageTile);
        }

        public void bind (int position) {
            image.setBackgroundColor(ContextCompat.getColor(context, getColor(position)));
        }

        private int getColor (int position) {
            int remainder = position % 5;
            switch (remainder) {
                case 0:
                    return android.R.color.holo_red_light;
                case 1:
                    return android.R.color.holo_orange_light;
                case 2:
                    return android.R.color.holo_green_light;
                case 3:
                    return android.R.color.holo_blue_light;
                case 4:
                    return android.R.color.holo_purple;
                default:
                    return android.R.color.black;
            }
        }
    }
}
