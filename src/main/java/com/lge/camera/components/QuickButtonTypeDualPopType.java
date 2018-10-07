package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeDualPopType extends QuickButtonType {
    public QuickButtonTypeDualPopType(boolean enable) {
        super(C0088R.id.quick_button_dual_pop_type, Setting.KEY_DUAL_POP_TYPE, -2, -2, true, false, new int[]{C0088R.string.mode_dual_pop_dual_capture, C0088R.string.mode_dual_pop_dual_capture_off}, enable, new int[]{C0088R.drawable.setting_dualpop_defalut_mode, C0088R.drawable.setting_dualpop_popout_mode}, new int[]{110, 111}, 0, null, 0);
    }
}
