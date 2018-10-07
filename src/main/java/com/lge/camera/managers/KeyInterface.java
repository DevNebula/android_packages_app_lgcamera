package com.lge.camera.managers;

import android.view.KeyEvent;

public interface KeyInterface {
    boolean doBackKey();

    boolean doDpadCenterKey(KeyEvent keyEvent);

    boolean doDpadKey(int i, KeyEvent keyEvent);

    boolean doHeadSetHookAndMediaKey(KeyEvent keyEvent);

    void doHotKeyUp();

    boolean doMenuKey();

    boolean doVolumeUpKey();

    boolean isEnableVolumeKey();

    void onCameraKeyUp(KeyEvent keyEvent);

    boolean onFingerPrintSensorDown(int i, KeyEvent keyEvent);

    boolean onShutterDown(int i, KeyEvent keyEvent);

    boolean onShutterUp(int i, KeyEvent keyEvent);

    boolean onVolumeKeyLongPressed(int i, KeyEvent keyEvent);
}
