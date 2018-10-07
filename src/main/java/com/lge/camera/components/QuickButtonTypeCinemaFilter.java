package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeCinemaFilter extends QuickButtonType {
    public QuickButtonTypeCinemaFilter(boolean enable) {
        super(C0088R.id.quick_button_cinema_filter, "", -2, -2, true, false, new int[]{C0088R.string.Advanced_beauty_filter, C0088R.string.Advanced_beauty_filter}, enable, new int[]{C0088R.drawable.btn_quickbutton_cine_filter_normal, C0088R.drawable.btn_quickbutton_cine_filter_pressed}, new int[]{99, 98}, 0, null, 0);
    }
}
