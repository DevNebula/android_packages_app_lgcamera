package com.nostra13.universalimageloader.core.assist;

import android.widget.ImageView;

public enum ViewScaleType {
    FIT_INSIDE,
    CROP;

    public static ViewScaleType fromImageView(ImageView imageView) {
        switch ($SWITCH_TABLE$android$widget$ImageView$ScaleType()[imageView.getScaleType().ordinal()]) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return FIT_INSIDE;
            default:
                return CROP;
        }
    }
}
