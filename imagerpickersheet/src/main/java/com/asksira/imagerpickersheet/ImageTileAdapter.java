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

public class ImageTileAdapter extends RecyclerView.Adapter<ImageTileAdapter.BaseViewHolder> {

    private static final int VIEWTYPE_CAMERA = 101;
    private static final int VIEWTYPE_GALLERY = 102;
    private static final int VIEWTYPE_IMAGE = 103;

    protected Context context;

    public ImageTileAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public ImageTileAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_CAMERA:
                return new CameraTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_camera_tile, parent, false));
            case VIEWTYPE_GALLERY:
                return new GalleryTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_gallery_tile, parent, false));
            default:
                return new ImageTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_tile, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ImageTileAdapter.BaseViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEWTYPE_CAMERA;
            case 1:
                return VIEWTYPE_GALLERY;
            default:
                return VIEWTYPE_IMAGE;
        }
    }

    public abstract static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(int position);

    }

    public class CameraTileViewHolder extends BaseViewHolder {

        public CameraTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Launch Camera Intent
                }
            });
        }

        @Override
        public void bind(int position) {

        }
    }

    public class GalleryTileViewHolder extends BaseViewHolder {

        public GalleryTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Launch Gallery Intent
                }
            });
        }

        @Override
        public void bind(int position) {

        }
    }

    public class ImageTileViewHolder extends BaseViewHolder {

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
