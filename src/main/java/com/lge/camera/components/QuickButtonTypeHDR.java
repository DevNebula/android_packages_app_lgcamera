package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeHDR extends QuickButtonType {
    public QuickButtonTypeHDR(boolean enable) {
        super(C0088R.id.quick_button_hdr, "hdr-mode", -2, -2, true, false, new int[]{C0088R.string.hdr_on, C0088R.string.hdr_off}, enable, new int[]{C0088R.drawable.btn_quickbutton_hdr_on_button, C0088R.drawable.btn_quickbutton_hdr_off_button}, new int[]{63, 62}, 0, null, 0);
        setStringId(new int[]{C0088R.string.title_hdr, C0088R.string.title_hdr});
    }
}
