package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class FlashOffIntentReceiver extends CameraBroadCastReceiver {
    public FlashOffIntentReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (this.mGet.isRecordingState() && this.mGet.isNeedToCheckFlashTemperature()) {
            CamLog.m3d(CameraConstants.TAG, "onReceive(), action :" + action);
            this.mGet.setFlashOffByHighTemperature(true);
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("com.lge.android.intent.action.CAMERA_FLASH_OFF");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
