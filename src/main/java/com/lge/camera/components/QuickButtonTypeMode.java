package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeMode extends QuickButtonType {
    public QuickButtonTypeMode(boolean enable) {
        int[] iArr = new int[2];
        super(C0088R.id.quick_button_mode, "", -2, -2, true, false, new int[]{C0088R.string.accessiblity_mode_button, C0088R.string.accessiblity_mode_button}, enable, new int[]{C0088R.drawable.btn_quickbutton_mode_menu_button, C0088R.drawable.btn_quickbutton_mode_menu_button}, new int[]{36, 34}, 0, null, 0, C0088R.drawable.btn_quickbutton_mode_pressed, true, new int[]{C0088R.string.camera_quick_button_index_mode, C0088R.string.camera_quick_button_index_mode}, null);
    }
}
