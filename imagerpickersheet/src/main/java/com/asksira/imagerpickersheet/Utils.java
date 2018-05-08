package com.asksira.imagerpickersheet;

import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {

    public static int dp2px (int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

}
