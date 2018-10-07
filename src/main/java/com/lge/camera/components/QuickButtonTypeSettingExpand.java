package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeSettingExpand extends QuickButtonType {
    public QuickButtonTypeSettingExpand(boolean enable) {
        super(C0088R.id.quick_button_setting_expand, "", -2, -2, true, false, new int[]{C0088R.string.open_settings, C0088R.string.close_settings}, enable, new int[]{C0088R.drawable.btn_quickbutton_setting_expand_button, C0088R.drawable.btn_quickbutton_setting_expand_button}, new int[]{32, 30}, 0, null, 0, C0088R.drawable.btn_quickbutton_setting_pressed);
    }
}
