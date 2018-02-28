package com.asksira.imagepickersheetdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.asksira.imagerpickersheet.ImagePickerSheetDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.entrance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerSheetDialog pickerDialog = new ImagePickerSheetDialog();
                pickerDialog.show(getSupportFragmentManager(), "picker");
            }
        });
    }
}
