package com.lge.camera.systeminput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class AccessoryIntentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "AccessoryIntentReceiver : onReceive()");
        if (intent == null) {
        }
    }
}
