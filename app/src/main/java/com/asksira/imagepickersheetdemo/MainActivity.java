package com.asksira.imagepickersheetdemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.asksira.imagerpickersheet.ImagePickerSheetDialog;
import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ImagePickerSheetDialog.OnSingleImageSelectedListener,
        ImagePickerSheetDialog.OnMultiImageSelectedListener{

    private ImageView ivImage1, ivImage2, ivImage3, ivImage4, ivImage5, ivImage6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImage1 = findViewById(R.id.iv_image1);
        ivImage2 = findViewById(R.id.iv_image2);
        ivImage3 = findViewById(R.id.iv_image3);
        ivImage4 = findViewById(R.id.iv_image4);
        ivImage5 = findViewById(R.id.iv_image5);
        ivImage6 = findViewById(R.id.iv_image6);
        findViewById(R.id.tv_single_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerSheetDialog pickerDialog = new ImagePickerSheetDialog.Builder("com.asksira.imagepickersheetdemo.fileprovider")
                        .setMaximumDisplayingImages(Integer.MAX_VALUE)
                        .hideCameraTile()
                        .hideGalleryTile()
                        .build();
                pickerDialog.show(getSupportFragmentManager(), "picker");
            }
        });
        findViewById(R.id.tv_multi_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerSheetDialog pickerDialog = new ImagePickerSheetDialog.Builder("com.asksira.imagepickersheetdemo.fileprovider")
                        .setMaximumDisplayingImages(Integer.MAX_VALUE)
                        .isMultiSelect()
                        .setMinimumMultiSelectCount(3)
                        .setMaximumMultiSelectCount(6)
                        .setMultiSelectBarBgColor(R.color.black50)
                        .setMultiSelectTextColor(android.R.color.white)
                        .setMultiSelectDoneTextColor(R.color.colorAccent)
                        .build();
                pickerDialog.show(getSupportFragmentManager(), "picker");
            }
        });
    }

    @Override
    public void onSingleImageSelected(Uri uri) {
        Glide.with(MainActivity.this).load(uri).into(ivImage2);
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList) {
        for (int i=0; i < uriList.size(); i++) {
            if (i >= 6) return;
            ImageView iv;
            switch (i) {
                case 0:
                    iv = ivImage1;
                    break;
                case 1:
                    iv = ivImage2;
                    break;
                case 2:
                    iv = ivImage3;
                    break;
                case 3:
                    iv = ivImage4;
                    break;
                case 4:
                    iv = ivImage5;
                    break;
                case 5:
                default:
                    iv = ivImage6;
            }
            Glide.with(this).load(uriList.get(i)).into(iv);
        }
    }
}
