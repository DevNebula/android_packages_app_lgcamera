package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypePictureSize extends QuickButtonType {
    public QuickButtonTypePictureSize(boolean enable) {
        super(C0088R.id.quick_setting_picture_size, "picture-size", -2, -2, true, false, new int[]{C0088R.string.photo_size}, enable, new int[]{C0088R.drawable.btn_quicksetting_picture_size_button}, new int[]{113}, 0, null, 0);
        setStringId(new int[]{C0088R.string.photo_size});
    }
}
