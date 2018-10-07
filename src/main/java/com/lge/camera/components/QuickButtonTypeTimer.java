package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeTimer extends QuickButtonType {
    public QuickButtonTypeTimer(boolean enable) {
        super(C0088R.id.quick_button_timer, Setting.KEY_TIMER, -2, -2, true, false, new int[]{C0088R.string.timer, C0088R.string.timer_3sec, C0088R.string.timer_10sec}, enable, new int[]{C0088R.drawable.btn_quickbutton_timer_off_button, C0088R.drawable.btn_quickbutton_timer_3_button, C0088R.drawable.btn_quickbutton_timer_10_button}, new int[]{102, 103, 104}, 0, null, 0);
        setStringId(new int[]{C0088R.string.timer, C0088R.string.timer, C0088R.string.timer});
    }
}
