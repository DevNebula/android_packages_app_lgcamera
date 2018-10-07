package com.lge.camera.util;

public abstract class ChildSettingRunnable {
    public abstract boolean checkChildAvailable();

    public abstract void runChildSettingMenu(Object obj, String str, String str2);

    public void runChildSettingMenu(Object prefObject, String key, String value, int clickedType) {
    }

    public void runChild(Object prefObject, String key, String value) {
        if (checkChildAvailable()) {
            runChildSettingMenu(prefObject, key, value);
        }
    }
}
