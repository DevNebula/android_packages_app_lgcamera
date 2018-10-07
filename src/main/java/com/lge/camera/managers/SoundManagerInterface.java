package com.lge.camera.managers;

import com.lge.camera.app.IActivityBase;

public interface SoundManagerInterface extends IActivityBase {
    Object getListPreference(String str);

    boolean isMMSIntent();
}
