package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.StorageProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;

public class MediaReceiver extends CameraBroadCastReceiver {
    public MediaReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        CamLog.m7i(CameraConstants.TAG, action + "data : " + intent.getDataString());
        if (checkOnReceive(intent.getDataString())) {
            try {
                if (action.equals("android.intent.action.MEDIA_EJECT") || action.equals("android.intent.action.MEDIA_REMOVED") || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                    doMediaBadRemoval();
                } else if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                    doMediaMounted(intent);
                } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
                    doMediaUnMounted();
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "CameraMediaBroadcastReceiver Exception : ", e);
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_EJECT");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        intentFilter.addAction("android.intent.action.MEDIA_REMOVED");
        intentFilter.addDataScheme("file");
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    private void doMediaUnMounted() {
    }

    private void doMediaMounted(Intent intent) {
        boolean z = false;
        CamLog.m7i(CameraConstants.TAG, "doMediaMounted");
        String path = intent.getDataString();
        if (path != null && StorageProperties.isMemoryMounted(this.mGet.getAppContext(), 0) && !this.mGet.isPaused()) {
            this.mGet.hideAllToast();
            if (checkStorage(path, 1)) {
                if (this.mGet.isRotateDialogVisible()) {
                    this.mGet.removeRotateDialog();
                }
                this.mGet.removeSettingMenu(true, false);
                this.mGet.hideModeMenu(false, false);
                this.mGet.hideHelpList(false, false);
                this.mGet.doInitSettingOrder();
                ReceiverInterface receiverInterface = this.mGet;
                String str = Setting.KEY_STORAGE;
                if (!this.mGet.isRequestedSingleImage()) {
                    z = true;
                }
                receiverInterface.setSettingMenuEnable(str, z);
                if (!this.mGet.isRequestedSingleImage()) {
                    this.mGet.showDialogPopup(8, true);
                }
                this.mGet.onSDcardInserted();
            } else if (checkStorage(path, 2)) {
                this.mGet.removeSettingMenu(true, false);
                this.mGet.doInitSettingOrder();
            }
        }
    }

    private void doMediaBadRemoval() {
        if (!this.mFinished) {
            if (!this.mGet.isPaused()) {
                this.mGet.hideAllToast();
            }
            this.mGet.getActivity().finish();
            this.mFinished = true;
        }
    }

    private boolean checkOnReceive(String dataSting) {
        if (this.mGet == null) {
            CamLog.m11w(CameraConstants.TAG, "mGet is null");
            return false;
        } else if (!dataSting.contains("USBstorage")) {
            return true;
        } else {
            CamLog.m11w(CameraConstants.TAG, "file:///storage/USBstrorage");
            return false;
        }
    }

    private boolean checkStorage(String path, int storageType) {
        if (path == null || !StorageProperties.isMemoryMounted(this.mGet.getAppContext(), storageType) || this.mGet.getStorageDir(storageType) == null) {
            return false;
        }
        return path.endsWith(this.mGet.getStorageDir(storageType));
    }
}
