package com.lge.camera.systeminput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ShortcutListManager;
import com.lge.camera.util.CamLog;

public class LocaleChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "System locale changed");
        if (FunctionProperties.isSupportedShortcut() && "android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
            ShortcutListManager.refreshShortcuts(context);
        }
    }
}
