package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeVideoSize extends QuickButtonType {
    public QuickButtonTypeVideoSize(boolean enable) {
        super(C0088R.id.quick_setting_video_size, Setting.KEY_VIDEO_RECORDSIZE, -2, -2, true, false, new int[]{C0088R.string.video_resolution}, enable, new int[]{C0088R.drawable.btn_quicksetting_video_size_button}, new int[]{114}, 0, null, 0);
        setStringId(new int[]{C0088R.string.video_size});
    }
}
