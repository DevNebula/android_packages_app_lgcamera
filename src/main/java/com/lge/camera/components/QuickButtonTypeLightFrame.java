package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeLightFrame extends QuickButtonType {
    public static final int INDEX_OFF = 0;
    public static final int INDEX_ON = 1;

    public QuickButtonTypeLightFrame(boolean enable, int width, int height) {
        super(C0088R.id.quick_button_light_frame, Setting.KEY_LIGHTFRAME, width, height, true, false, new int[]{C0088R.string.switch_to_flash_front, C0088R.string.switch_to_flash_front}, enable, new int[]{C0088R.drawable.btn_quickbutton_flash_off_button, C0088R.drawable.btn_quickbutton_flash_on_button}, new int[]{56, 57}, 0, null, 0);
        setAnimationDrwableIds(new int[]{C0088R.drawable.avd_flash_off_on, C0088R.drawable.avd_flash_on_off});
    }
}
