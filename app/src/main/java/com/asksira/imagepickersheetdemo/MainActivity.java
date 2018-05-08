package com.asksira.imagepickersheetdemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.asksira.imagerpickersheet.ImagePickerSheetDialog;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity implements ImagePickerSheetDialog.OnSingleImageSelectedListener {

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImage = findViewById(R.id.iv_image);
        findViewById(R.id.entrance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerSheetDialog pickerDialog = new ImagePickerSheetDialog.Builder("com.asksira.imagepickersheetdemo.fileprovider")
                        .build();
                pickerDialog.show(getSupportFragmentManager(), "picker");
            }
        });
    }

    @Override
    public void onSingleImageSelected(Uri uri) {
        Glide.with(MainActivity.this).load(uri).into(ivImage);
    }
}
