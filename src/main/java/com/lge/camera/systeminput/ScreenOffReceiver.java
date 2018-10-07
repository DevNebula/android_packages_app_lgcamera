package com.lge.camera.systeminput;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class ScreenOffReceiver extends CameraBroadCastReceiver {
    public ScreenOffReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        Activity activity = this.mGet.getActivity();
        if (activity != null && !activity.isFinishing()) {
            CamLog.m3d(CameraConstants.TAG, "onReceived, android.intent.action.SCREEN_OFF");
            PowerManager pm = (PowerManager) activity.getSystemService("power");
            if (pm != null) {
                CamLog.m3d(CameraConstants.TAG, "isScreenOn : " + pm.isScreenOn());
                if (!pm.isScreenOn()) {
                    activity.finish();
                }
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("android.intent.action.SCREEN_OFF");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
