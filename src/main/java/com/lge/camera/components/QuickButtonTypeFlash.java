package com.lge.camera.components;

import com.lge.camera.C0088R;

public class QuickButtonTypeFlash extends QuickButtonType {
    public QuickButtonTypeFlash(boolean enable, int width, int height) {
        super(C0088R.id.quick_button_flash, "flash-mode", width, height, true, false, new int[]{C0088R.string.switch_to_flash_on_button, C0088R.string.switch_to_flash_on_button, C0088R.string.switch_to_flash_on_button}, enable, new int[]{C0088R.drawable.btn_quickbutton_flash_off_button, C0088R.drawable.btn_quickbutton_flash_on_button, C0088R.drawable.btn_quickbutton_flash_auto_button}, new int[]{50, 51, 52}, 0, null, 0);
        setAnimationDrwableIds(new int[]{C0088R.drawable.avd_flash_off_on, -1, C0088R.drawable.avd_flash_on_off});
    }
}
