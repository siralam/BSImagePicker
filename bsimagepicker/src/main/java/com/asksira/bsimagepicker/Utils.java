package com.asksira.bsimagepicker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;

public class Utils {

    public static int dp2px (int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void checkPermission (Fragment fragment, String permissionString, int permissionCode) {
        if ((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || fragment.getContext() == null) return;
        int existingPermissionStatus = ContextCompat.checkSelfPermission(fragment.getContext(),
                permissionString);
        if (existingPermissionStatus == PackageManager.PERMISSION_GRANTED) return;
        fragment.requestPermissions(new String[]{permissionString}, permissionCode);
    }

    public static boolean isReadStorageGranted (Context context) {
        int storagePermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return storagePermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isWriteStorageGranted (Context context) {
        int storagePermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return storagePermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCameraGranted (Context context) {
        int cameraPermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        return cameraPermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

}
