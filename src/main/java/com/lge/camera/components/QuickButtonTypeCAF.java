package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeCAF extends QuickButtonType {
    public static final int INDEX_ON = 0;

    public QuickButtonTypeCAF(boolean enable) {
        super(C0088R.id.quick_button_caf, "", -2, -2, true, false, new int[]{C0088R.string.camera_accessibility_caf_button}, enable, new int[]{C0088R.drawable.btn_quickbutton_caf_button}, new int[]{58}, 0, null, 0);
    }
}
