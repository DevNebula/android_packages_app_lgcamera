package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeBackKey extends QuickButtonType {
    public QuickButtonTypeBackKey(boolean enable) {
        super(C0088R.id.quick_button_back_key, "", -2, -2, true, false, new int[]{C0088R.string.camera_accessibility_back_button}, enable, new int[]{C0088R.drawable.btn_quickbutton_backkey}, new int[]{59}, 0, null, 0);
    }
}
