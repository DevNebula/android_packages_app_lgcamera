package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeColorEffect extends QuickButtonType {
    public QuickButtonTypeColorEffect(boolean enable) {
        super(C0088R.id.quick_button_color_effect, Setting.KEY_COLOR_EFFECT, -2, -2, true, false, new int[]{C0088R.string.Advanced_beauty_filter, C0088R.string.Advanced_beauty_filter}, enable, new int[]{C0088R.drawable.btn_quickbutton_filter_off, C0088R.drawable.btn_quickbutton_filter_off}, new int[]{101, 100}, 0, null, 0, C0088R.drawable.btn_quickbutton_filter_pressed);
    }
}
