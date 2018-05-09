package com.asksira.imagerpickersheet;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
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
    protected boolean isMultiSelect;
    protected List<File> selectedFiles;

    private View.OnClickListener cameraTileOnClickListener;
    private View.OnClickListener galleryTileOnClickListener;
    private View.OnClickListener imageTileOnClickListener;

    public interface OnSelectedCountChangeListener {
        void onSelectedCountChange (int currentCount);
    }
    private OnSelectedCountChangeListener onSelectedCountChangeListener;

    public ImageTileAdapter(Context context, boolean isMultiSelect) {
        super();
        this.context = context;
        this.isMultiSelect = isMultiSelect;
        selectedFiles = new ArrayList<>();
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
        if (!isMultiSelect) {
            return imageList == null ? 2 : 2 + imageList.size();
        } else {
            return imageList == null ? 1 : 1 + imageList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return isMultiSelect ? VIEWTYPE_GALLERY : VIEWTYPE_CAMERA;
            case 1:
                return isMultiSelect ? VIEWTYPE_IMAGE : VIEWTYPE_GALLERY;
            default:
                return VIEWTYPE_IMAGE;
        }
    }

    public List<Uri> getSelectedUris () {
        List<Uri> result = new ArrayList<>();
        for (File each : selectedFiles) {
            result.add(Uri.fromFile(each));
        }
        return result;
    }

    public void setImageList (List<File> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
    }

    public void setCameraTileOnClickListener(View.OnClickListener cameraTileOnClickListener) {
        this.cameraTileOnClickListener = cameraTileOnClickListener;
    }

    public void setGalleryTileOnClickListener(View.OnClickListener galleryTileOnClickListener) {
        this.galleryTileOnClickListener = galleryTileOnClickListener;
    }

    public void setImageTileOnClickListener(View.OnClickListener imageTileOnClickListener) {
        this.imageTileOnClickListener = imageTileOnClickListener;
    }

    public void setOnSelectedCountChangeListener(OnSelectedCountChangeListener onSelectedCountChangeListener) {
        this.onSelectedCountChangeListener = onSelectedCountChangeListener;
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
            itemView.setOnClickListener(cameraTileOnClickListener);
        }

        @Override
        public void bind(int position) {

        }
    }

    public class GalleryTileViewHolder extends BaseViewHolder {

        public GalleryTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(galleryTileOnClickListener);
        }

        @Override
        public void bind(int position) {

        }
    }

    public class ImageTileViewHolder extends BaseViewHolder {

        View darken;
        ImageView ivImage, ivTick;

        public ImageTileViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.item_imageTile);
            darken = itemView.findViewById(R.id.imageTile_selected_darken);
            ivTick = itemView.findViewById(R.id.imageTile_selected);
            if (!isMultiSelect) {
                itemView.setOnClickListener(imageTileOnClickListener);
            } else {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File thisFile = imageList.get(getAdapterPosition() - 1);
                        if (selectedFiles.contains(thisFile)) {
                            selectedFiles.remove(thisFile);
                            notifyItemChanged(getAdapterPosition());
                        } else {
                            selectedFiles.add(thisFile);
                            notifyItemChanged(getAdapterPosition());
                        }
                        if (onSelectedCountChangeListener != null) {
                            onSelectedCountChangeListener.onSelectedCountChange(selectedFiles.size());
                        }
                    }
                });
            }
        }

        public void bind (int position) {
            if (imageList == null) return;
            File imageFile = imageList.get(position - (isMultiSelect ? 1 : 2));
            itemView.setTag(Uri.fromFile(imageFile));
            Glide.with(itemView).load(imageFile).into(ivImage);
            darken.setVisibility(selectedFiles.contains(imageFile)? View.VISIBLE : View.INVISIBLE);
            ivTick.setVisibility(selectedFiles.contains(imageFile)? View.VISIBLE : View.INVISIBLE);
        }
    }
}
