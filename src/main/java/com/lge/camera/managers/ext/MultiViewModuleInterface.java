package com.lge.camera.managers.ext;

import com.lge.camera.managers.ModuleInterface;

public interface MultiViewModuleInterface extends ModuleInterface {
    void changeLayoutOnMultiview(String str);

    boolean isCameraChanging();

    boolean isMultiviewIntervalShot();

    void restoreSettingValue(String str);

    void setCameraChangingOnSnap(boolean z);
}
