package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeMultiviewInterval extends QuickButtonType {
    public QuickButtonTypeMultiviewInterval(boolean enable) {
        super(C0088R.id.quick_button_multi_frameshot, Setting.KEY_MULTIVIEW_FRAMESHOT, -2, -2, true, false, new int[]{C0088R.string.multiview_frameshot_on_talkback, C0088R.string.multiview_frameshot_off_talkback}, enable, new int[]{C0088R.drawable.setting_integrate_parent_multiview_sequential, C0088R.drawable.setting_integrate_parent_multiview_same_time}, new int[]{80, 81}, 0, null, 0);
    }
}
