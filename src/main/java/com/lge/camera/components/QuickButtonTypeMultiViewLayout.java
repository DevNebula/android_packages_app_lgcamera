package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeMultiViewLayout extends QuickButtonType {
    public QuickButtonTypeMultiViewLayout(boolean enable) {
        super(C0088R.id.quick_button_multi_view_layout, Setting.KEY_MULTIVIEW_LAYOUT, -2, -2, true, false, new int[]{C0088R.string.multiview_layout_single, C0088R.string.multiview_layout_splitview, C0088R.string.multiview_layout_tripleview01, C0088R.string.multiview_layout_tripleview02, C0088R.string.multiview_layout_quadview}, enable, new int[]{C0088R.drawable.setting_multiview_layout_single, C0088R.drawable.setting_multiview_layout_split, C0088R.drawable.setting_multiview_layout_triple1, C0088R.drawable.setting_multiview_layout_triple2, C0088R.drawable.setting_multiview_layout_quad}, new int[]{41}, 2, null, 0, C0088R.drawable.camera_icon_setting_multiview_three_01_pressed, true);
    }
}
