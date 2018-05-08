package com.asksira.imagerpickersheet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * The RecyclerView's adapter of the selectable ivImage tiles.
 */

public class ImageTileAdapter extends RecyclerView.Adapter<ImageTileAdapter.BaseViewHolder> {

    private static final int VIEWTYPE_CAMERA = 101;
    private static final int VIEWTYPE_GALLERY = 102;
    private static final int VIEWTYPE_IMAGE = 103;

    protected Context context;
    protected List<File> imageList;

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
        return imageList == null ? 2 : 2 + imageList.size();
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

    public void setImageList (List<File> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
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

        ImageView ivImage;

        public ImageTileViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.item_imageTile);
        }

        public void bind (int position) {
            if (imageList == null) return;
            File imageFile = imageList.get(position - 2);
            Glide.with(itemView).load(imageFile).into(ivImage);
        }
    }
}
