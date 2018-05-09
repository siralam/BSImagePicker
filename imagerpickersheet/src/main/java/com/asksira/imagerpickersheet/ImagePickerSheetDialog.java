package com.asksira.imagerpickersheet;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * This is the core class of this library, which extends BottomSheetDialogFragment
 * from the design support library, in order to provide the basic architecture of a bottom sheet.
 * <p>
 * It is also responsible for:
 * - Handling permission
 * - Communicate with caller activity / fragment
 * - As a view controller
 */

public class ImagePickerSheetDialog extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1000;

    private static final int PERMISSION_READ_STORAGE = 2001;
    private static final int PERMISSION_CAMERA = 2002;
    private static final int PERMISSION_WRITE_STORAGE = 2003;

    private static final int REQUEST_TAKE_PHOTO = 3001;
    private static final int REQUEST_SELECT_FROM_GALLERY = 3002;

    //Views
    private RecyclerView recyclerView;
    private View bottomBarView;
    private TextView tvDone, tvMultiSelectMessage;

    private BottomSheetBehavior bottomSheetBehavior;

    //Components
    private ImageTileAdapter adapter;

    //Callbacks
    public interface OnSingleImageSelectedListener {
        void onSingleImageSelected(Uri uri);
    }
    private OnSingleImageSelectedListener onSingleImageSelectedListener;
    public interface OnMultiImageSelectedListener {
        void onMultiImageSelected (List<Uri> uriList);
    }
    private OnMultiImageSelectedListener onMultiImageSelectedListener;

    //States
    private boolean isMultiSelection = false;
    private Uri currentPhotoUri;

    //Configurations
    private int maximumDisplayingImages = 50;
    private int peekHeight = Utils.dp2px(360);
    private int minimumMultiSelectCount = 1;
    private int maximumMultiSelectCount = Integer.MAX_VALUE;
    private String providerAuthority;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSingleImageSelectedListener) {
            onSingleImageSelectedListener = (OnSingleImageSelectedListener) context;
        }
        if (context instanceof OnMultiImageSelectedListener) {
            onMultiImageSelectedListener = (OnMultiImageSelectedListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadConfigFromBuilder();
        if (Utils.isReadStorageGranted(getContext())) {
            getLoaderManager().initLoader(LOADER_ID, null, ImagePickerSheetDialog.this);
        } else {
            Utils.checkPermission(ImagePickerSheetDialog.this, Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_STORAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_picker_sheet, container, false);
        bindViews(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Get the BottomSheetBehavior
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                    bottomSheetBehavior.setPeekHeight(peekHeight);
                    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                        @Override
                        public void onStateChanged(@NonNull View bottomSheet, int newState) {
                            switch (newState) {
                                case BottomSheetBehavior.STATE_HIDDEN:
                                    dismiss();
                                    break;
                            }
                        }

                        @Override
                        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                            if (bottomBarView != null) {
                                bottomBarView.setAlpha(slideOffset < 0 ? (1 + slideOffset) : 1);
                            }
                        }
                    });
                }
            }
        });

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isMultiSelection) {
            recyclerView.setPadding(0, 0, 0, Utils.dp2px(48));
            setupBottomBar(getView());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (getContext() == null) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        switch (requestCode) {
            case PERMISSION_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLoaderManager().initLoader(LOADER_ID, null, this);
                } else {
                    dismiss();
                }
                break;
            case PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Utils.isWriteStorageGranted(getContext())) {
                        launchCamera();
                    } else {
                        Utils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_WRITE_STORAGE);
                    }
                }
            case PERMISSION_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Utils.isCameraGranted(getContext())) {
                        launchCamera();
                    } else {
                        Utils.checkPermission(this, Manifest.permission.CAMERA, PERMISSION_CAMERA);
                    }
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    notifyGallery();
                    if (onSingleImageSelectedListener != null) {
                        onSingleImageSelectedListener.onSingleImageSelected(currentPhotoUri);
                        dismiss();
                    }
                } else {
                    try {
                        File file = new File(URI.create(currentPhotoUri.toString()));
                        file.delete();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            Log.d("ImagePicker", "Failed to delete temp file: " + currentPhotoUri.toString());
                    }
                }
                break;
            case REQUEST_SELECT_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    if (onSingleImageSelectedListener != null) {
                        onSingleImageSelectedListener.onSingleImageSelected(data.getData());
                        dismiss();
                    }
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID && getContext() != null) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{MediaStore.Images.Media.DATA};
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
            return new CursorLoader(getContext(), uri, projection, null, null, sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            List<File> uriList = new ArrayList<>();
            int index = 0;
            while (cursor.moveToNext() && index < maximumDisplayingImages) {
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                uriList.add(new File(imagePath));
                index++;
            }
            cursor.moveToFirst();
            adapter.setImageList(uriList);
            //We are not closing the cursor here because Android Doc says Loader will manage them.
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.setImageList(null);
    }

    private void bindViews(View rootView) {
        recyclerView = rootView.findViewById(R.id.picker_recyclerview);
    }

    private void loadConfigFromBuilder() {
        try {
            providerAuthority = getArguments().getString("providerAuthority");
            isMultiSelection = getArguments().getBoolean("isMultiSelect");
            maximumDisplayingImages = getArguments().getInt("maximumDisplayingImages");
            minimumMultiSelectCount = getArguments().getInt("minimumMultiSelectCount");
            maximumMultiSelectCount = getArguments().getInt("maximumMultiSelectCount");
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gll = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gll);
        /* We are disabling item change animation because the default animation is fade out fade in, which will
         * appear a little bit strange due to the fact that we are darkening the cell at the same time. */
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.addItemDecoration(new GridItemSpacingDecoration(3, Utils.dp2px(2), false));
        if (adapter == null) {
            adapter = new ImageTileAdapter(getContext(), isMultiSelection);
            adapter.setMaximumSelectionCount(maximumMultiSelectCount);
            adapter.setCameraTileOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isCameraGranted(getContext()) && Utils.isWriteStorageGranted(getContext())) {
                        launchCamera();
                    } else {
                        if (Utils.isCameraGranted(getContext())) {
                            Utils.checkPermission(ImagePickerSheetDialog.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_WRITE_STORAGE);
                        } else {
                            Utils.checkPermission(ImagePickerSheetDialog.this, Manifest.permission.CAMERA, PERMISSION_CAMERA);
                        }
                    }
                }
            });
            adapter.setGalleryTileOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_FROM_GALLERY);
                }
            });
            adapter.setImageTileOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() != null && v.getTag() instanceof Uri && onSingleImageSelectedListener != null) {
                        onSingleImageSelectedListener.onSingleImageSelected((Uri) v.getTag());
                        dismiss();
                    }
                }
            });
            if (isMultiSelection) {
                adapter.setOnSelectedCountChangeListener(new ImageTileAdapter.OnSelectedCountChangeListener() {
                    @Override
                    public void onSelectedCountChange(int currentCount) {
                        updateSelectCount(currentCount);
                    }
                });
                adapter.setOnOverSelectListener(new ImageTileAdapter.OnOverSelectListener() {
                    @Override
                    public void onOverSelect() {
                        showOverSelectMessage();
                    }
                });
            }
        }
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomBar(View rootView) {
        CoordinatorLayout parentView = (CoordinatorLayout) (rootView.getParent().getParent());
        bottomBarView = LayoutInflater.from(getContext()).inflate(R.layout.item_selection_bar, parentView, false);
        ViewCompat.setTranslationZ(bottomBarView, ViewCompat.getZ((View) rootView.getParent()));
        parentView.addView(bottomBarView, -1);
        tvMultiSelectMessage = bottomBarView.findViewById(R.id.tv_multiselect_message);
        tvMultiSelectMessage.setText(getString(R.string.imagepicker_multiselect_not_enough_singular));
        tvDone = bottomBarView.findViewById(R.id.tv_multiselect_done);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMultiImageSelectedListener != null) {
                    onMultiImageSelectedListener.onMultiImageSelected(adapter.getSelectedUris());
                    dismiss();
                }
            }
        });
        tvDone.setAlpha(0.4f);
        tvDone.setEnabled(false);
    }

    private void launchCamera() {
        if (getContext() == null) return;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                if (BuildConfig.DEBUG) e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        providerAuthority,
                        photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoUri = Uri.fromFile(image);
        return image;
    }

    private void notifyGallery() {
        if (getContext() == null) return;
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(currentPhotoUri);
        getContext().sendBroadcast(mediaScanIntent);
    }

    private void updateSelectCount (int newCount) {
        if (getContext() == null) return;
        if (tvMultiSelectMessage != null) {
            tvMultiSelectMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
            if (newCount < minimumMultiSelectCount) {
                tvMultiSelectMessage.setText(minimumMultiSelectCount - newCount == 1 ?
                        getString(R.string.imagepicker_multiselect_not_enough_singular) :
                        getString(R.string.imagepicker_multiselect_not_enough_plural, minimumMultiSelectCount - newCount));
                tvDone.setAlpha(0.4f);
                tvDone.setEnabled(false);
            } else {
                tvMultiSelectMessage.setText(newCount == 1 ?
                        getString(R.string.imagepicker_multiselect_enough_singular) :
                        getString(R.string.imagepicker_multiselect_enough_plural, newCount));
                tvDone.setAlpha(1f);
                tvDone.setEnabled(true);
            }
        }
    }

    private void showOverSelectMessage () {
        if (tvMultiSelectMessage != null && getContext() != null) {
            tvMultiSelectMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.error_text));
            tvMultiSelectMessage.setText(getString(R.string.imagepicker_multiselect_overselect, maximumMultiSelectCount));
        }
    }

    /**
     * Builder of the ImagePickerSheetDialog.
     * Caller should always create the dialog using this builder.
     */
    public static class Builder {

        private String providerAuthority;
        private boolean isMultiSelect;
        private int maximumDisplayingImages = 50;
        private int minimumMultiSelectCount = 1;
        private int maximumMultiSelectCount = Integer.MAX_VALUE;

        public Builder(String providerAuthority) {
            this.providerAuthority = providerAuthority;
        }

        public Builder isMultiSelect () {
            isMultiSelect = true;
            return this;
        }

        public Builder setMaximumDisplayingImages (int maximumDisplayingImages) {
            this.maximumDisplayingImages = maximumDisplayingImages;
            return this;
        }

        public Builder setMinimumMultiSelectCount(int minimumMultiSelectCount) {
            this.minimumMultiSelectCount = minimumMultiSelectCount;
            return this;
        }

        public Builder setMaximumMultiSelectCount(int maximumMultiSelectCount) {
            this.maximumMultiSelectCount = maximumMultiSelectCount;
            return this;
        }

        public ImagePickerSheetDialog build() {
            Bundle args = new Bundle();
            args.putString("providerAuthority", providerAuthority);
            args.putBoolean("isMultiSelect", isMultiSelect);
            args.putInt("maximumDisplayingImages", maximumDisplayingImages);
            args.putInt("minimumMultiSelectCount", minimumMultiSelectCount);
            args.putInt("maximumMultiSelectCount", maximumMultiSelectCount);


            ImagePickerSheetDialog fragment = new ImagePickerSheetDialog();
            fragment.setArguments(args);
            return fragment;
        }

    }
}
