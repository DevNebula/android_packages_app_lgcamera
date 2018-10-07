package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeCAFFocusable extends QuickButtonType {
    public static final int INDEX_ON = 0;

    public QuickButtonTypeCAFFocusable(boolean enable) {
        super(C0088R.id.quick_button_caf, "", -2, -2, true, true, new int[]{C0088R.string.camera_accessibility_caf_button}, enable, new int[]{C0088R.drawable.btn_quickbutton_caf_button}, new int[]{58}, 0, null, 0);
    }
}
