package com.asksira.imagerpickersheet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is the core class of this library, which extends BottomSheetDialogFragment
 * from the design support library, in order to provide the basic architecture of a bottom sheet.
 *
 * It is also responsible for:
 * - Handling permission
 * - Communicate with caller activity / fragment
 * - As a view controller
 */

public class ImagePickerSheetDialog extends BottomSheetDialogFragment {

    //Views
    RecyclerView recyclerView;


    //Adapter
    ImageTileAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_picker_sheet, container, false);
        bindViews(view);

        setupRecyclerView();

        return view;
    }

    private void bindViews (View rootView) {
        recyclerView = rootView.findViewById(R.id.picker_recyclerview);
    }

    private void setupRecyclerView () {
        GridLayoutManager gll = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gll);
        recyclerView.addItemDecoration(new GridItemSpacingDecoration(3, 5, false));
        adapter = new ImageTileAdapter(getContext());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Builder of the ImagePickerSheetDialog.
     * Caller should always create the dialog using this builder.
     */
    public class Builder {


    }
}
