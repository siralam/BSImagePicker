package com.asksira.imagerpickersheet;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the core class of this library, which extends BottomSheetDialogFragment
 * from the design support library, in order to provide the basic architecture of a bottom sheet.
 *
 * It is also responsible for:
 * - Handling permission
 * - Communicate with caller activity / fragment
 * - As a view controller
 */

public class ImagePickerSheetDialog extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1000;

    private static final int PERMISSION_READ_STORAGE = 2001;
    private static final int PERMISSION_CAMERA = 2002;

    //Views
    private RecyclerView recyclerView;
    private View bottomBarView;

    private BottomSheetBehavior bottomSheetBehavior;

    //Components
    private ImageTileAdapter adapter;

    //States
    private boolean isMultiSelection = false;

    //Configurations
    private int maximumDisplayingImages = 50;
    private int peekHeight = Utils.dp2px(360);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        switch (requestCode) {
            case PERMISSION_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLoaderManager().initLoader(LOADER_ID, null, this);
                } else {
                    dismiss();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void bindViews (View rootView) {
        recyclerView = rootView.findViewById(R.id.picker_recyclerview);
    }

    private void setupRecyclerView () {
        GridLayoutManager gll = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gll);
        recyclerView.addItemDecoration(new GridItemSpacingDecoration(3, Utils.dp2px(2), false));
        adapter = new ImageTileAdapter(getContext());
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomBar (View rootView) {
        CoordinatorLayout parentView = (CoordinatorLayout) (rootView.getParent().getParent());
        bottomBarView = LayoutInflater.from(getContext()).inflate(R.layout.item_selection_bar, parentView, false);
        ViewCompat.setTranslationZ(bottomBarView, ViewCompat.getZ((View)rootView.getParent()));
        parentView.addView(bottomBarView, -1);
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

    /**
     * Builder of the ImagePickerSheetDialog.
     * Caller should always create the dialog using this builder.
     */
    public class Builder {


    }
}
