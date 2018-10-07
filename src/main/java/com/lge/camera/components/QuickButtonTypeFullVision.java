package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeFullVision extends QuickButtonType {
    public QuickButtonTypeFullVision(boolean enable) {
        super(C0088R.id.quick_setting_full_vision, Setting.KEY_FULLVISION, -2, -2, true, false, new int[]{C0088R.string.quick_setting_title_fullvision, C0088R.string.quick_setting_title_fullvision}, enable, new int[]{C0088R.drawable.btn_quicksetting_full_vision_off_button, C0088R.drawable.btn_quicksetting_full_vision_on_button}, new int[]{116, 115}, 0, null, 0);
        setStringId(new int[]{C0088R.string.quick_setting_title_fullvision, C0088R.string.quick_setting_title_fullvision});
    }
}
