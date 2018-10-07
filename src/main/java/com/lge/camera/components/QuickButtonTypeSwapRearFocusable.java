package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeSwapRearFocusable extends QuickButtonType {
    public QuickButtonTypeSwapRearFocusable(boolean enable) {
        super(C0088R.id.quick_button_swap_camera_focusable, Setting.KEY_SWAP_CAMERA, -2, -2, true, true, new int[]{C0088R.string.switch_to_front_camera}, enable, new int[]{C0088R.drawable.btn_quickbutton_swap_button}, new int[]{6}, 0, null, 0, 0, false);
    }
}
