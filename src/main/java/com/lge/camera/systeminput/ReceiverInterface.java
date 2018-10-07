package com.lge.camera.systeminput;

import android.net.Uri;

public interface ReceiverInterface extends ReceiverInterfaceBase {
    void cameraPolicyChanged();

    void changeSettingValueInMdm();

    void doBLEOneKeyAction(boolean z);

    boolean isActivityPaused();

    boolean isNeedToCheckFlashTemperature();

    boolean isRequestedSingleImage();

    boolean isScreenPinningState();

    void onBTAudioConnectionStateChanged(boolean z);

    void onBTConnectionStateChanged(boolean z);

    void onBTStateChanged(boolean z);

    void onDualConnectionDeviceTypeChanged(int i);

    void onReceiveAudioNoiseIntent();

    void onReceiveAudioRaMIntent(int i);

    void refreshUspZone(boolean z);

    void setDeletedUriFromGallery(Uri uri);

    void setFlashOffByHighTemperature(boolean z);
}
